package com.philips.research.licensescanner.core.domain.license;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LicenseTest {
    private static final String IDENTIFIER = "Identifier";
    private static final String EXCEPTION = "Exception";

    @Nested
    class ProgrammaticConstruction {
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
            final var one = License.of("A");
            final var two = License.of("B");
            final var three = License.of("C");

            assertThat(one.or(two).toString()).isEqualTo("(A OR B)");
            assertThat((one.or(two)).or(three).toString()).isEqualTo("(A OR B OR C)");
            assertThat(one.or(two.or(three)).toString()).isEqualTo("(A OR B OR C)");
        }

        @Test
        void combinesLicensesUsingAnd() {
            final var one = License.of("A");
            final var two = License.of("B");
            final var three = License.of("C");

            assertThat(one.and(two).toString()).isEqualTo("(A AND B)");
            assertThat((one.and(two)).and(three).toString()).isEqualTo("(A AND B AND C)");
            assertThat(one.and(two.and(three)).toString()).isEqualTo("(A AND B AND C)");
        }

        @Test
        void combinesComboLicenses() {
            final var one = License.of("A");
            final var two = License.of("B");
            final var three = License.of("C");

            assertThat((one.or(two)).and(three).toString()).isEqualTo("((A OR B) AND C)");
            assertThat(one.or(two.and(three)).toString()).isEqualTo("((B AND C) OR A)");
            assertThat((one.and(two)).or(three).toString()).isEqualTo("((A AND B) OR C)");
            assertThat(one.and(two.or(three)).toString()).isEqualTo("((B OR C) AND A)");
        }
    }

    @Nested
    class Equality {
        @Test
        void implementsHash() {
            final var license = License.of(IDENTIFIER);

            assertThat(license.hashCode()).isNotNull();
            assertThat(license.hashCode()).isNotEqualTo(License.of("Other").hashCode());
        }

        @Test
        void implementsEquals() {
            final var license = License.of(IDENTIFIER);

            assertThat(license).isEqualTo(license);
            assertThat(license).isNotEqualTo(null);
            assertThat(license).isEqualTo(License.of(IDENTIFIER));
            //noinspection AssertBetweenInconvertibleTypes
            assertThat(license).isNotEqualTo("42");
            assertThat(License.of("A").and(License.of("B"))).isEqualTo(License.of("B").and(License.of("A")));
        }
    }
}


