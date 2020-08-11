package com.philips.research.licensescanner.core.domain.license;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LicenseParserTest {
    private static final String IDENTIFIER = "Identifier";
    private static final String EXCEPTION = "Exception";

    @Test
    void parsesNoLicense() {
        assertThat(LicenseParser.parse("")).isEmpty();
        assertThat(LicenseParser.parse("   ")).isEmpty();
        assertThat(LicenseParser.parse("( )")).isEmpty();
    }

    @Test
    void parsesSingleLicense() {
        var license = LicenseParser.parse(IDENTIFIER);

        assertThat(license).contains(License.of(IDENTIFIER));
    }

    @Test
    void withExceptionClause() {
        var license = LicenseParser.parse(IDENTIFIER + " with " + EXCEPTION);

        assertThat(license).contains(License.of(IDENTIFIER).with(EXCEPTION));
    }

    @Test
    void parsesSingleBracketedLicense() {
        var license = LicenseParser.parse("(" + IDENTIFIER+ ")");

        assertThat(license).contains(License.of(IDENTIFIER));
    }

    @Test
    void throws_rogueWithClause() {
        assertThatThrownBy(()-> LicenseParser.parse("with E"))
                .isInstanceOf(LicenseException.class)
        .hasMessageContaining("WITH");
    }

    @Test
    void throws_doubleWithClause() {
        assertThatThrownBy(()-> LicenseParser.parse("A with B with C"))
                .isInstanceOf(LicenseException.class);
    }

    @Test
    void parsesOrCombination() {
        var license = LicenseParser.parse("A or B or C");

        assertThat(license).contains(License.of("A").or(License.of("B")).or(License.of("C")));
    }

    @Test
    void parsesAndCombination() {
        var license = LicenseParser.parse("A and B and C");

        assertThat(license).contains(License.of("A").and(License.of("B")).and(License.of("C")));
    }

    @Test
    void parsesMixedLogicCombination() {
        var license = LicenseParser.parse("A or B and C");

        assertThat(license).contains(License.of("A").or(License.of("B")).and(License.of("C")));
    }

    @Test
    void throws_licensesWithoutLogicalOperator() {
        assertThatThrownBy(()-> LicenseParser.parse("A B"))
                .isInstanceOf(LicenseException.class)
        .hasMessageContaining("logical operator");
    }

    @Test
    void addsWithClauseToLatestParsedLicense() {
        var license = LicenseParser.parse("A or B with E and C");

        assertThat(license).contains(License.of("A").or(License.of("B").with("E")).and(License.of("C")));
    }

    @Test
    void parsesBracketedCombination() {
        var license = LicenseParser.parse("A or (B and C)");

        assertThat(license).contains(License.of("A").or(License.of("B").and(License.of("C"))));
    }

    @Test
    void throws_unbalancedOpenBracket() {
        assertThatThrownBy(()-> LicenseParser.parse("("))
                .isInstanceOf(LicenseException.class)
                .hasMessageContaining("opening bracket");
    }

    @Test
    void throws_unbalancedClosingBracket() {
        assertThatThrownBy(()-> LicenseParser.parse(")"))
                .isInstanceOf(LicenseException.class)
                .hasMessageContaining("closing bracket");
    }

    @Test
    void throws_withClauseFollowedByOpeningBracket() {
        assertThatThrownBy(()-> LicenseParser.parse("A with (something)"))
                .isInstanceOf(LicenseException.class)
                .hasMessageContaining("not expected");
    }
}
