package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.ScanError;
import com.philips.research.licensescanner.core.domain.Package;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Spring JPA query definitions for scanning errors.
 */
public interface ScanErrorRepository extends CrudRepository< ScanErrorEntity, Long> {
    List<ScanError> findAllByPkgOrderByTimestampDesc(Package pkg);
}
