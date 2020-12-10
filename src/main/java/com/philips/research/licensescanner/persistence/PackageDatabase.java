/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.PackageStore;
import com.philips.research.licensescanner.core.domain.Package;
import com.philips.research.licensescanner.core.domain.Scan;
import org.springframework.stereotype.Repository;
import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring component implementing the persistence of packages.
 */
@Repository
public class PackageDatabase implements PackageStore {
    @SuppressWarnings("NotNullFieldNotInitialized")
    static DetectionRepository detectionRepository;

    private final PackageRepository packageRepository;
    private final ScanRepository scanRepository;

    public PackageDatabase(PackageRepository packageRepository, ScanRepository scanRepository,
                           DetectionRepository detectionRepository) {
        this.packageRepository = packageRepository;
        this.scanRepository = scanRepository;
        PackageDatabase.detectionRepository = detectionRepository;
    }

    @Override
    public Package createPackage(URI purl) {
        final var entity = new PackageEntity(purl);
        return packageRepository.save(entity);
    }

    @Override
    public Optional<Package> getPackage(URI purl) {
        final var slim = PackageEntity.toSlim(purl);
        return packageRepository.findBySlimPurl(slim).map(pkg -> pkg);
    }

    @Override
    public List<Package> findPackages(String namespace, String name, String version) {
        var mask = (name.isBlank()) ? "%" : wildcard(name);
        if (!namespace.isBlank()) {
            mask = wildcard(namespace) + "/" + mask;
        }
        if (!version.isBlank()) {
            mask += "@" + wildcard(version);
        }
        return new ArrayList<>(packageRepository.findTop50BySlimPurlLikeOrderBySlimPurlAsc(mask));
    }

    private String wildcard(String name) {
        return '%' + name + '%';
    }

    @Override
    public void deletePackage(Package pkg) {
        scanRepository.deleteByPkg(pkg);
        packageRepository.delete((PackageEntity) pkg);
    }

    @Override
    public Scan createScan(Package pkg, @NullOr URI location) {
        final var entity = new ScanEntity((PackageEntity) pkg, location);
        return scanRepository.save(entity);
    }

    @Override
    public Optional<Scan> latestScan(Package pkg) {
        return scanRepository.findTopByPkgOrderByIdDesc((PackageEntity) pkg).map(scan -> scan);
    }

    @Override
    public void deleteScan(Scan scan) {
        scanRepository.delete((ScanEntity) scan);
    }

    @Override
    public List<Scan> scanErrors(Package pkg) {
        var list = scanRepository.findAllByPkgAndErrorIsNotNullOrderByTimestampDesc((PackageEntity) pkg);
        return toScans(list);
    }

    @Override
    public List<Scan> scanErrors() {
        return toScans(scanRepository.findFirst100ByErrorIsNotNullOrderByTimestampDesc());
    }

    @Override
    public List<Scan> contested() {
        return toScans(scanRepository.findFirst100ByContestingIsNotNullOrderByTimestampDesc());
    }

    @Override
    public Optional<Scan> getScan(UUID scanId) {
        return scanRepository.findByUuid(scanId).map(scan -> scan);
    }

    @Override
    public List<Scan> findScans(Instant from, Instant until) {
        var list = scanRepository.findTop50ByTimestampGreaterThanEqualAndTimestampLessThanEqualOrderByTimestampDesc(from, until);
        return toScans(list);
    }

    @Override
    public int countLicenses() {
        return scanRepository.countByErrorIsNull();
    }

    @Override
    public int countErrors() {
        return scanRepository.countByErrorIsNotNull();
    }

    @Override
    public int countContested() {
        return scanRepository.countByContestingIsNotNull();
    }

    private List<Scan> toScans(List<ScanEntity> list) {
        //noinspection unchecked
        return (List<Scan>) (Object) list;
    }
}

