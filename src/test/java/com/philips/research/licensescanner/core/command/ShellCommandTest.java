/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.licensescanner.core.command;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShellCommandTest {
    @Test
    void executesCommand() {
        // Throws if not executed in Maven root directory
        new ShellCommand("cd")
                .execute("src");
    }

    @Test
    void executesCommandInDirectory() {
        // Throws if not executed in Maven "src" directory
        new ShellCommand("cd")
                .setDirectory(new File("src"))
                .execute("test");
    }

    @Test
    void throws_commandNotFound() {
        assertThatThrownBy(() -> new ShellCommand("not_found").execute())
                .isInstanceOf(ShellException.class)
                .hasMessageContaining("Command 'not_found' not found");
    }

    @Test
    void throws_commandStatusNotSuccess() {
        assertThatThrownBy(() -> new ShellCommand("cat").execute("not_a_file.txt"))
                .isInstanceOf(ShellException.class)
                .hasMessageContaining("failed with status 1");
    }

    @Test
    void throws_commandTimedOut() {
        assertThatThrownBy(() -> new ShellCommand("read").setTimeout(Duration.ofSeconds(0)).execute())
                .isInstanceOf(ShellException.class)
                .hasMessageContaining("Aborted 'read' after");
    }
}
