package com.philips.research.licensescanner.core.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Shell command invocation with status handling.
 */
public class ShellCommand {
    private static final Logger LOG = LoggerFactory.getLogger(ShellCommand.class);

    private final String command;
    private File directory = new File(".");
    private Duration timeout = Duration.ofSeconds(30);

    public ShellCommand(String command) {
        this.command = command;
    }

    /**
     * @param directory New working directory
     */
    public ShellCommand setDirectory(File directory) {
        this.directory = directory;
        return this;
    }

    /**
     * @param timeout Maximum duration of the command.
     *                (Defaults to 30 seconds)
     */
    public ShellCommand setTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Performs the command with the provided arguments.
     *
     * @param args arguments
     */
    public ShellCommand execute(Object... args) {
        execute(List.of(args));
        return this;
    }

    /**
     * Performs the command with the provided arguments.
     *
     * @param args arguments
     */
    public void execute(Iterable<Object> args) {
        try {
            LOG.info("Run {} {}", command, args);

            Process process = invoke(args);
            assertSuccessStatus(process);
        } catch (IOException e) {
            throw new ShellException("Command '" + command + "' not found", e);
        } catch (InterruptedException e) {
            throw new ShellException("Waiting to command '" + command + "' was interrupted", e);
        }
    }

    private Process invoke(Iterable<Object> args) throws IOException, InterruptedException {
        var process = new ProcessBuilder()
                .directory(directory)
                .command(invocationArguments(args)).start();
        if (!process.waitFor(timeout.toSeconds(), TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new ShellException("Aborted '" + command + "' after " + timeout.toSeconds() + " seconds");
        }
        return process;
    }

    private List<String> invocationArguments(Iterable<Object> arguments) {
        var result = new ArrayList<String>();
        result.add(command);
        arguments.forEach(obj -> result.add(obj.toString()));
        return result;
    }

    private void assertSuccessStatus(Process process) {
        var status = process.exitValue();
        if (status != 0) {
            throw new ShellException("Command failed with status " + status);
        }
    }
}
