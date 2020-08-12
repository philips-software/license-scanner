package com.philips.research.licensescanner.core.domain.license;

import java.util.Optional;

/**
 * Parser for SPDX-like license statements containing AND, OR, WITH clauses and braces.
 */
public class LicenseParser {
    private StringBuilder buffer = new StringBuilder();
    private License license = null;
    private License latest = null;
    private Mode mode = Mode.NONE;

    private LicenseParser() {
    }

    /**
     * @return the license matching the provided text
     */
    public static Optional<License> parse(String text) {
        return new LicenseParser().decode(text);
    }

    private Optional<License> decode(String text) {
        for (var i = 0; i < text.length(); i++) {
            final var ch = text.charAt(i);
            switch (ch) {
                case '(':
                    final var sub = bracketSubstring(text,i+1);
                    new LicenseParser().decode(sub).ifPresent(lic -> {
                        switch (mode) {
                            case AND:
                                license = license.and(lic);
                                break;
                            case OR:
                                license = license.or(lic);
                                break;
                            case NONE:
                                license = lic;
                                break;
                            default:
                                throw new LicenseException("Opening bracket is not expected");
                        }
                    });
                    i += sub.length() + 1;
                    break;
                case ')':
                    throw new LicenseException("Unbalanced closing bracket found");
                case ' ':
                    parseToken();
                    break;
                default:
                    buffer.append(ch);
            }
        }
        parseToken();
        return Optional.ofNullable(license);
    }

    private String bracketSubstring(String text, int start) {
        var nested = 0;
        for (var i = start; i < text.length(); i++) {
            final var ch = text.charAt(i);
            if (ch == '(') {
                nested++;
            } else if (ch == ')') {
                if (nested == 0) {
                    return text.substring(start, i);
                }
                nested--;
            }
        }
        throw new LicenseException("Unbalanced opening bracket found");
    }

    private void parseToken() {
        final var token = buffer.toString();
        if (token.isEmpty()) {
            return;
        }

        switch (token.toLowerCase()) {
            case "with":
                mode = Mode.WITH;
                break;
            case "and":
                mode = Mode.AND;
                break;
            case "or":
                mode = Mode.OR;
                break;
            default:
                appendToken(token);
        }
        buffer = new StringBuilder();
    }

    private void appendToken(String token) {
        switch (mode) {
            case WITH:
                if (latest == null) {
                    throw new LicenseException("No license for WITH clause");
                }
                latest.with(token);
                break;
            case AND:
                license = license.and(createSingle(token));
                break;
            case OR:
                license = license.or(createSingle(token));
                break;
            default:
                if (latest != null) {
                    throw new LicenseException("Missing logical operator between " + latest + " and " + token);
                }
                license = createSingle(token);
        }
        mode = Mode.NONE;
    }

    private License createSingle(String name) {
        latest = License.of(name);
        return latest;
    }

    enum Mode {NONE, WITH, AND, OR}
}

