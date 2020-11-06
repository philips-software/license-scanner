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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ShellCommandTest {
    @Test
    void executesCommand() {
        new ShellCommand("ls")
                .execute("-lah");
    }

    @Test
    void executesCommandInDirectory() {
        new ShellCommand("ls")
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
        assertThatThrownBy(() -> new ShellCommand("sleep").execute("5").setTimeout(Duration.ofSeconds(0)).execute())
                .isInstanceOf(ShellException.class)
                .hasMessageContaining("Aborted 'sleep' after");
    }
}
