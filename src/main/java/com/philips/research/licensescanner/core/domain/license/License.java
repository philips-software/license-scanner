package com.philips.research.licensescanner.core.domain.license;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

abstract public class License {
    @SuppressWarnings("StaticInitializerReferencesSubClass")
    public static final License NONE = new NoLicense();

    public static License of(String identifier) {
        if (identifier.isBlank()) {
            return NONE;
        }
        return new SingleLicense(identifier);
    }

    public License with(String exception) {
        throw new LicenseException("Cannot add WITH clause to '" + this + "'");
    }

    public License and(License license) {
        if (license instanceof NoLicense) {
            return this;
        }
        return new AndLicense(this).and(license);
    }

    public License or(License license) {
        if (license instanceof NoLicense) {
            return this;
        }
        return new OrLicense(this).or(license);
    }

    @Override
    public final int hashCode() {
        return toString().hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof License)) {
            return false;
        }
        return this.getClass() == obj.getClass()
                && toString().equals(obj.toString());
    }

    public boolean isDefined() {
        return true;
    }

    private static class NoLicense extends License {
        @Override
        public boolean isDefined() {
            return false;
        }

        @Override
        public License and(License license) {
            return license;
        }

        @Override
        public License or(License license) {
            return license;
        }

        @Override
        public String toString() {
            return "";
        }
    }

    private static class SingleLicense extends License {
        private final String identifier;

        private String exception = null;

        SingleLicense(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public License with(String exception) {
            if (this.exception != null) {
                throw new LicenseException("Adding a second exception is not allowed");
            }
            this.exception = exception;
            return this;
        }

        @Override
        public String toString() {
            return (exception != null)
                    ? String.format("%s WITH %s", identifier, exception)
                    : identifier;
        }
    }

    private static class ComboLicense extends License {
        private final String operation;
        private final List<License> licenses = new ArrayList<>();

        public ComboLicense(String operation, License license) {
            this.operation = String.format(" %s ", operation);
            licenses.add(license);
        }

        License merge(License license) {
            if (license.getClass() == this.getClass()) {
                licenses.addAll(((ComboLicense) license).licenses);
            } else if (!(license instanceof NoLicense)) {
                licenses.add(license);
            }
            return this;
        }

        @Override
        public String toString() {
            return licenses.stream()
                    .map(Object::toString)
                    .sorted()
                    .collect(Collectors.joining(operation, "(", ")"));
        }
    }

    private static class OrLicense extends ComboLicense {
        public OrLicense(License license) {
            super("OR", license);
        }

        @Override
        public License or(License license) {
            return merge(license);
        }
    }

    private static class AndLicense extends ComboLicense {
        public AndLicense(License license) {
            super("AND", license);
        }

        @Override
        public License and(License license) {
            return merge(license);
        }
    }
}
