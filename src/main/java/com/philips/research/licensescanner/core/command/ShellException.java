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
