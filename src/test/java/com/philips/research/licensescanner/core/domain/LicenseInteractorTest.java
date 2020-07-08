package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.domain.download.Downloader;
import com.philips.research.licensescanner.core.domain.license.Detector;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class LicenseInteractorTest {
    private final Downloader downloader = mock(Downloader.class);
    private final Detector detector = mock(Detector.class);

    @Test
    void createsInstance() {
        new LicenseInteractor(downloader, detector);
    }


}
