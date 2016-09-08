# github-companion

A small tool which helps larger teams to work with GitHub.

## Installation

Download from https://github.com/whitepages/github-companion.

## Usage

Run the project directly:

    $ boot run

Run the project's tests:

    $ boot test

Build an uberjar from the project:

    $ boot build

Run the uberjar:

    $ java -jar target/github-companion-0.1.0-SNAPSHOT.jar [args]

## Commands

The command option which works now is the `grant`.

## Options

### Authorization options

`-a` or `--auth` USERNAME:PASSWORD
`-o` or `--oauth-token` TOKEN

### Enterprise options

`-u` or `--url` URL

## Examples

    $ java -jar target/github-companion-0.1.0-SNAPSHOT.jar --url https://github.dev.pages/api/v3/ --oauth-token 0123456789abcdef0123456789abcdef01234567 grant org/team-slurp

## License

Copyright © 2016 Whitepages Inc.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
