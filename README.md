# License Scanner service
**Description**: Wrapper around a source code license scanner to make scan results 
available via an API, and provide a user interface for manual curation of scan result.

Typical usage is the integration with CI/CD build pipeline tools (like 
[SPDX-Builder](https://github.com/philips-labs/spdx-builder)) to retrieve available 
license information and trigger asynchronous scanning of (versions of) packages that 
were not already scanned. 

The actual scanning of software license statements from source code is performed by 
the [ScanCode Toolkit](https://github.com/nexB/scancode-toolkit) version 3.x command 
line tool. The service schedules scanning of packages in the background, making 
scan results available the next time they are requested.

Scanning a package technically starts by downloading its source code, and then 
invokes an external license scanner to extract the license information. 

License scanners report detected licenses per source file, which are joined by the 
service into a single package-level license using the logical "AND" operator.
The API reports licenses as-is, without checking for validity or compatibility. 
In case of dual licensing, it is left to the client to choose the appropriate 
license.

Current supported sources for downloading source code from:
- Plain web URL download
- Git 2.24 or higher

**Status**: Research prototype

## Dependencies

The service requires the Java 11 (or later) runtime environment.

The H2 database implementation is part of the application, so no external 
dependencies are (currently) required for persistent storage.

## Installation

The application is build using the standard Maven build command:
```
mvn clean install
```
The resulting JAR is created in the `target` directory.

## Configuration

### ScanCode Toolkit installation
ScanCode Toolkit must be invoked on Linux and OSX using an absolute
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

## Usage

The service can be started from the command line:
```
java -jar license-scanner-service.jar
```

The service exposes on port 8080:
* An API to interact with the scanning service
* A user interface on [localhost:8080/](http://localhost:80080) to monitor license 
scanning errors and manually curate scanned licenses. (See the separate 
[license-scanner-ui](https://github.com/philips-labs/license-scanner-ui) 
user interface project.)
* A simple database management tool on [localhost:8080/h2](http://localhost:8080/h2)
with credentials "user" and "password".

If migration of the database fails, a stand-alone can be started from the 
command line on Linux or Mac using:

    java -jar ~/.m2/repository/com/h2database/h2/<version>/h2-<version>.jar
    
(Migrations can be manually fixed or removed in the "flyway_schema_history" 
table.)

## How to test the software

The unit test suite can be executed via Maven:
```
mvn clean test
```

## Known issues
(Checked items are under development.)

Must-have
- [ ] Add package manager type to package identifier. (Or use "purl" instead?)
- [ ] Production-grade database (e.g. Postgres).
- [ ] Authentication of clients.

Should-have
- [ ] Support (confirmed) declared non-SPDX custom licenses.
- [ ] Verify checksum of downloaded artifact before scanning.
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

## Contact / Getting help

Use the issue tracker of this project.

## License

See [LICENSE.md](LICENSE.md).

## Credits and references

1. Documentation for [ScanCode Toolkit](https://readthedocs.org/projects/scancode-toolkit).
