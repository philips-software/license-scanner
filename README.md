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
ScanCode Toolkit requires to be invoked on Linux and OSX using the 
installation path (see [documuntation](https://scancode-toolkit.readthedocs.io/en/latest/cli-reference/synopsis.html)).
When not installed using pip or running on Windows, make sure `extractcode` and 
`scancode` can be accessed through a script, without providing the installation 
path.

Package source code gets downloaded to a temporary directory for scanning.
The base directory is the `TMPDIR` directory, and can be changed by setting 
the `LICENSE_DIR` environment variable.

## TO DO / Limitations
- List all individually scanned licenses instead of joining them with an 
"AND" operator.
- Include SPDX license list version handling. (Request and response?)
- Include license name, text, and reference URL as dictionary for all non-SPDX 
references mentioned in the licenses list.
- Detect and return copyright statements.
- Store (any) provided declared license, so manual curation can take it into 
account.
- (Client and) API for curation of scan results and restarting failed scans.
- The service stores licenses in a H2 database to avoid integration with an 
external database. For production this needs to be a more reliable and faster 
database.
- An in-memory thread pool queues scans that does not persist upon a server 
restart. This should be replace by a persistent AMQP queue.
