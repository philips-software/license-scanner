/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.core.command;

/**
 * Exception thrown for failures while invoking a shell command.
 */
public class ShellException extends RuntimeException {
    public ShellException(String message) {
        super(message);
    }

    public ShellException(String message, Throwable cause) {
        super(message, cause);
    }
}
