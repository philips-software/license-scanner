# License Scanner service

Backend service to scan licenses from the source code of (open source) packages.

**Status**: _Experimental research prototype_

(See the [architecture document](docs/architecture.md) in the `docs` directory.)

Typical usage is the integration with CI/CD build pipeline tools (like
[SPDX-Builder](https://github.com/philips-software/spdx-builder)) to obtain
validated license information. Prior scan results are provided if the package
has been scanned before. Packages that were not yet scanned are automatically
scheduled for download and scanning.
A [web user interface](https://github.com/philips-software/license-scanner-ui)
is provisioned by the the service for monitoring the scanning process and
manually curate scan results.

Clients interact with the service via a REST API to provide the license for the
specified package. If the package was scanned before, the license is returned
immediately. Else a scan of the package is scheduled if a source code location
was provided, so a future request for the package can be answered. When a client
detects a mismatch between the license declared by (e.g.) a package manager and
the license detected by the scanner, it can "contest" the license. This marks
the scanned license for human curation. After manual inspection, the (corrected)
license is "confirmed" to indicate the next requesting client that the provided
license is reliable. If the scan failed due to an incorrect source code location
or other technical issue, the user can manually correct the location and restart
the scan.

The [ScanCode Toolkit version 3.x](https://github.com/nexB/scancode-toolkit)
command line tool performs the actual source code scan. The service schedules
download of package source code in the background, invokes the scanner, and
makes detected license information available the next time it is requested by a
client via the REST API.

ScanCode Toolkit reports detected licenses per source file, which are joined by
the service into a single package-level license using the logical "AND"
operator. The API reports licenses as-is, without checking for validity or
compatibility. In case of dual licensing, it is left to the client to choose the
appropriate license.

The service persists per detected license:

- The total number of detections in the code
- A sample file with a (largest) range of lines that indicated the license
- The license itself, specified in (where
  possible) [SDPX identifiers](https://spdx.org/licenses)

Manual curation allows for marking individual detections as false-positives, and
adjusting the confirmed license accordingly. In the user interface the button
next to the source location opens a web URL that is derived from the VCS URI (
see below) to manually browse the referenced source archive or download the
source code archive.

Packages are specified by
their [package URL](https://github.com/package-url/purl-spec)
to ensure unique identification across package managers.

The location of source code is specified using a VCS location URI according to
the format defined in
the [SPDX specification](https://spdx.github.io/spdx-spec/3-package-information/#37-package-download-location):

```
<vcs_tool>+<transport>://<host_name>[/<path_to_repository>][@<revision_tag_or_branch>][#<sub_path>]
```

where all fields are URL-encoded to escape reserved characters (like "@" to "
%40").

Current supported sources for downloading source code from:

- Plain web (and file) URL download
- Installed command-line Git client (version 2.24 or higher)

In case of a plain download, the downloaded archive is automatically extracted
before starting the scan.

The Git download assumes the default branch if no explicit version is provided.
Else it attempts to check out the source code in the following ways:

1. Branch/tag checkout using the literal version
2. Branch/tag checkout prepending "v" to the literal version
3. Revision checkout using the literal version as commit hash

## Dependencies

The service requires the Java 11 (or later) runtime environment.

Scan results are persisted to disk in a local H2 database. The H2 database
driver is part of the application, so no external dependencies are
(currently) required for persistent storage.

## Installation

The application is built from source code using the standard Gradle build
command:

```
gradlew build
```

The `build/distributions` directory contains archives for distribution of the
Java application, including startup scripts.

## Configuration

### ScanCode Toolkit installation

ScanCode Toolkit must be invoked on Linux and OSX using an absolute installation
path (
see [the ScanCode Toolkit documentation](https://scancode-toolkit.readthedocs.io/en/latest/cli-reference/synopsis.html))
. When not installed using pip or running on Windows, make sure `extractcode`
and
`scancode` can be accessed through a script, without providing the installation
path.

### Working directory for temporary files

Package source code gets downloaded to a temporary directory for scanning. The
base directory is the `TMPDIR` directory, and can be changed by setting
the `LICENSE_DIR` environment variable.

### License detection threshold

The heuristic processes detecting licenses from source code use a default
certainty threshold of 50 (percent) to accept a detected license. This threshold
can be overridden using the `LICENSE_THRESHOLD` environment variable to set a
value between 0 and 100.

## Usage

The service can be started from the command line using the startup scripts in
the
`bin` directory of the distribution archive.

After starting up, the service exposes on port 8080:

* An API to interact with the scanning service
* A user interface on [localhost:8080/](http://localhost:80080) to monitor
  license scanning errors and manually curate scanned licenses. (See the
  separate
  [license-scanner-ui](https://github.com/philips-software/license-scanner-ui)
  user interface project.)
* A simple database management tool
  on [localhost:8080/h2](http://localhost:8080/h2)
  with credentials "user" and "password".

If migration of the database fails, a stand-alone can be started from the
command line on Linux or Mac using:

    java -jar ~/.m2/repository/com/h2database/h2/<version>/h2-<version>.jar

(Failed migrations can be manually fixed or removed in the "
flyway_schema_history"
table.)

### Docker

After building the project, you can also run the application with Docker.

Use docker-compose:

```bash
docker-compose up -d
```

Use image stored on docker hub :

```bash
docker run -p 8080:8080 philipssoftware/license-scanner:latest
```

Build docker image:

```bash
  docker build -f docker/Dockerfile -t license-scanner .
```

Run application:

```bash
docker run -p 8080:8080 license-scanner 
```

## How to test the software

The unit test suite can be executed via Gradle:

```
gradlew test
```

## Known issues

(Checked items are under development.)

Must-have

- [ ] Authentication of client edits to prevent unauthorized curations.

Should-have

- [ ] Make number of processes configurable to improve performance on (virtual)
  machines with fewer cores.
- [ ] Download by commit hash instead of git clone, because this is much faster.
- [ ] Detect and return copyright statements.
- [ ] Production-grade database (e.g. Postgres).

## Contact / Getting help

Use the issue tracker of this project.

## License

See [LICENSE.md](LICENSE.md).

## Credits and references

This service could not be made without the ScanCode Toolkit project. See the
[documentation of ScanCode Toolkit](https://readthedocs.org/projects/scancode-toolkit)
for details on its invocation and how it detects licenses in source code files.
