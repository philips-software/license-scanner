package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.Detection;
import com.philips.research.licensescanner.core.domain.license.License;

import javax.persistence.*;

@Entity
@Table(name = "detections")
public class DetectionEntity extends Detection {
    @ManyToOne
    @JoinColumn(name = "scan_id")
    private final ScanEntity scan;

    @Id
    @GeneratedValue
    private Long id;

    public DetectionEntity() {
        this(null, null);
    }

    public DetectionEntity(ScanEntity scan, License license) {
        super(license);
        this.scan = scan;
    }
}
