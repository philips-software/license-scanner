package com.philips.research.licensescanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LicenseScannerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LicenseScannerServiceApplication.class, args);
    }
}
