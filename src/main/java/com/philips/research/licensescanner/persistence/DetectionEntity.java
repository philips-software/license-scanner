package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.Detection;
import com.philips.research.licensescanner.core.domain.license.License;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "detections")
public class DetectionEntity extends Detection {
    @Id
    @GeneratedValue
    private Long id;

    public DetectionEntity() {
        this(null);
    }

    public DetectionEntity(License license) {
        super(license);
    }
}
