package com.philips.research.licensescanner.persistence;

import org.springframework.data.repository.CrudRepository;

interface ScanRepository extends CrudRepository<ScanEntity, Long> {
}
