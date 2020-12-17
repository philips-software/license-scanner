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

import com.philips.research.licensescanner.core.ScanStore;
import com.philips.research.licensescanner.core.domain.Scan;
import org.springframework.stereotype.Repository;
import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Spring component implementing the persistence of packages.
 */
@Repository
public class ScanDatabase implements ScanStore {
    @SuppressWarnings("NotNullFieldNotInitialized")
    static DetectionRepository detectionRepository;

    private final ScanRepository scanRepository;

    public ScanDatabase(ScanRepository scanRepository, DetectionRepository detectionRepository) {
        this.scanRepository = scanRepository;
        ScanDatabase.detectionRepository = detectionRepository;
    }

    @Override
    public List<Scan> findScans(String namespace, String name, String version) {
        var mask = (name.isBlank()) ? "%" : wildcard(name);
        if (!namespace.isBlank()) {
            mask = wildcard(namespace) + "/" + mask;
        }
        if (!version.isBlank()) {
            mask += "@" + wildcard(version);
        }
        return new ArrayList<>(scanRepository.findTop50BySearchLikeOrderByPurlAsc(mask));
    }

    private String wildcard(String name) {
        return '%' + name + '%';
    }

    @Override
    public Scan createScan(URI purl, @NullOr URI location) {
        final var entity = new ScanEntity(purl, location);
        return scanRepository.save(entity);
    }

    @Override
    public Optional<Scan> getScan(URI purl) {
        return scanRepository.findFirstByPurlOrderByTimestampDesc(purl).map(scan -> scan);
    }

    @Override
    public void deleteScan(Scan scan) {
        scanRepository.delete((ScanEntity) scan);
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
    public List<Scan> findScans(Instant from, Instant until) {
        var list = scanRepository.findTop100ByTimestampGreaterThanEqualAndTimestampLessThanEqualOrderByTimestampDesc(from, until);
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

