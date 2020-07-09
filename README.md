# License Scanner Service
## Overview
The License Scanner Service is a service for scanning licenses from package 
source code. This is accomplished by first downloading the sources of the
requested package, and then invoking a license scanner to detect the licenses
mentioned in the source files.

Supported version control systems:
- Git 2.24 or higher

Supported license scanners:
- ScanCode 3.0.x and 3.1.x

## Configuration
Package source code gets downloaded to a temporary directory for scanning.
The base directory is the `TMPDIR` directory, and can be changed by setting 
the `LICENSE_DIR` environment variable.
