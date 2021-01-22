/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.core.domain.download;

import com.philips.research.licensescanner.core.BusinessException;

/**
 * Exception thrown in case downloading of package sources failed.
 */
public class DownloadException extends BusinessException {
    public DownloadException(String message) {
        super(message);
    }

    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
