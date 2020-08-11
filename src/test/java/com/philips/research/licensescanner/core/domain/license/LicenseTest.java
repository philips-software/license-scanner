package com.philips.research.licensescanner.core.domain.license;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LicenseTest {
    private static final String IDENTIFIER = "Identifier";
    private static final String EXCEPTION = "Exception";

    @Test
    void createsSingleLicense() {
        var license = License.of(IDENTIFIER);

        assertThat(license.toString()).isEqualTo(IDENTIFIER);
    }

    @Test
    void addsExceptionToSingleLicense() {
        var license = License.of(IDENTIFIER).with(EXCEPTION);

        assertThat(license.toString()).isEqualTo(IDENTIFIER + " WITH " + EXCEPTION);
    }

    @Test
    void throws_doubleWithLicense() {
        assertThatThrownBy(() -> License.of(IDENTIFIER).with(EXCEPTION).with(EXCEPTION))
                .isInstanceOf(LicenseException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    void throws_withOnNonSingleLicense() {
        assertThatThrownBy(() -> License.of(IDENTIFIER).and(License.of("Other")).with(EXCEPTION))
                .isInstanceOf(LicenseException.class)
                .hasMessageContaining("Cannot add WITH");
    }

    @Test
    void combinesLicensesUsingOr() {
        final var one = License.of("one");
        final var two = License.of("two");
        final var three = License.of("three");

        assertThat(one.or(two).toString()).isEqualTo("(one OR two)");
        assertThat((one.or(two)).or(three).toString()).isEqualTo("(one OR two OR three)");
        assertThat(one.or(two.or(three)).toString()).isEqualTo("(one OR two OR three)");
    }

    @Test
    void combinesLicensesUsingAnd() {
        final var one = License.of("one");
        final var two = License.of("two");
        final var three = License.of("three");

        assertThat(one.and(two).toString()).isEqualTo("(one AND two)");
        assertThat((one.and(two)).and(three).toString()).isEqualTo("(one AND two AND three)");
        assertThat(one.and(two.and(three)).toString()).isEqualTo("(one AND two AND three)");
    }

    @Test
    void combinesComboLicenses() {
        final var one = License.of("one");
        final var two = License.of("two");
        final var three = License.of("three");

        assertThat((one.or(two)).and(three).toString()).isEqualTo("((one OR two) AND three)");
        assertThat(one.or(two.and(three)).toString()).isEqualTo("(one OR (two AND three))");
        assertThat((one.and(two)).or(three).toString()).isEqualTo("((one AND two) OR three)");
        assertThat(one.and(two.or(three)).toString()).isEqualTo("(one AND (two OR three))");
    }
}


