# License Scanner Service
## Overview
The "License Scanner Service" is a backend service for offline scanning and managing
copyright and license based on package source code. It is used by CI/CD pipelines to 
obtain curated license information for inclusion in Software Bill of Materials (SBOM) 
outputs. 

The service exposes a REST API for CI/CD build pipeline tools (like 
[SPDX-Builder](https://github.com/philips-labs/spdx-builder)) to retrieve available 
license information and trigger asynchronous scanning of (versions of) packages that 
were not already scanned. A client API is available for a manually curating licenses
and addressing scanning errors.

Scanning a package starts by downloading its source code, and then invokes an installed 
license scanner to extract the license information. (At the moment only [ScanCode
Toolkit](https://github.com/nexB/scancode-toolkit) is supported, but support for [FOSSology](https://github.com/fossology/fossology) 
or other scanners should be relatively easy to add.) 

Supported version control systems:
- Plain web URL download
- Git 2.24 or higher

Supported license scanners:
- ScanCode Toolkit 3.0.x and 3.1.x

The REST API is accessible on port 8080, and the service includes a user interface
for curation of scanned licenses. (See [license-scanner-ui](https://github.com/philips-labs/license-scanner-ui))
Additionally, the integrated H2 database is available on [localhost:8080/h2](http://localhost:8080/h2).

## Interpretation of detected licenses
License scanners report detected licenses per source file, which are joined by the 
service into a single package-level license using the logical "AND" operator.
The API reports licenses as-is, without checking for validity or compatibility. 
In case of dual licensing, it is left to the client to choose the appropriate 
license.

## Configuration
### ScanCode Toolkit installation
ScanCode Toolkit requires to be invoked on Linux and OSX using an absolute
installation path (see [the ScanCode Toolkit documentation](https://scancode-toolkit.readthedocs.io/en/latest/cli-reference/synopsis.html)).
When not installed using pip or running on Windows, make sure `extractcode` and 
`scancode` can be accessed through a script, without providing the installation 
path.

### Working directory for temporary files
Package source code gets downloaded to a temporary directory for scanning.
The base directory is the `TMPDIR` directory, and can be changed by setting 
the `LICENSE_DIR` environment variable.

### License detection threshold
The heuristic processes detecting licenses from source code use a default 
certainty threshold of 50 (percent) to accept a detected license. This threshold
can be overridden using the `LICENSE_THRESHOLD` environment variable to set a 
value between 0 and 100.

## TO DO / Limitations
(Checked items are under development.)

Must-have
- [ ] Add package manager type to package identifier. (Or use "purl" instead?)
- [x] Keep file and line information of scanned licenses in the database.
- [x] Explicit manual curation of scanned licenses to override false-positives.
- [x] Tracking and manual resolution of failed scans.
- [x] User interface for monitoring, management, and manual curation.
- [ ] Production-grade database (e.g. Postgres).
- [ ] Authentication of clients.

Should-have
- [ ] Support (confirmed) declared non-SPDX custom licenses.
- [ ] Verify checksum for downloaded artifact before scanning.
- [ ] API to "challenge" a package license based on a mismatch with the 
declared license.
- [ ] Detect and return other copyright statements.
- [ ] provide scanning time budget based on number of files.
- [ ] Remove SPDX licenses that are subsumed (=implied) by remaining licenses 

Others
- [ ] Integrate with FOSSology scanner.
- [ ] Include SPDX version handling.
- [ ] Multi-server scanning queue (like AMQP) that persists after restart of 
individual servers in a load-balanced configuration.

## Development
### Accessing the H2 database
The H2 database exposes its (simple) web client at path `/h2` with 
credentials "user" and "password".

If migration of the database fails, a stand-alone can be started from the 
command line on Linux or Mac using:

    java -jar ~/.m2/repository/com/h2database/h2/<version>/h2-<version>.jar
    
(Migrations can be manually fixed or removed in the "flyway_schema_history" 
table.)
