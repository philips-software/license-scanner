package com.philips.research.licensescanner.core.domain.license;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

abstract public class License {
    public static License of(String identifier) {
        return new SingleLicense(identifier);
    }

    public License with(String exception) {
        throw new LicenseException("Cannot add WITH clause to '" + this + "'");
    }

    public License and(License license) {
        return new AndLicense(this).and(license);
    }

    public License or(License license) {
        return new OrLicense(this).or(license);
    }

    private static class SingleLicense extends License {
        private final String identifier;

        SingleLicense(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public License with(String exception) {
            return new ExceptionLicense(this, exception);
        }

        @Override
        public String toString() {
            return identifier;
        }
    }

    private static class ExceptionLicense extends SingleLicense {
        private final String exception;

        ExceptionLicense(SingleLicense license, String exception) {
            super(license.toString());
            this.exception = exception;
        }

        @Override
        public License with(String exception) {
            throw new LicenseException("Adding a second exception is not allowed");
        }

        @Override
        public String toString() {
            return String.format("%s WITH %s", super.toString(), exception);
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
            } else {
                licenses.add(license);
            }
            return this;
        }

        @Override
        public String toString() {
            return licenses.stream()
                    .map(Object::toString)
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
