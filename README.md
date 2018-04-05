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

 - `grant` - Grants access to the selected team on your forks.
 - `list-teams` - List teams in the selected organization.
 - `protect` - Secures a repository.
 - `protect-team` - Secures all repositories of a team.

## Options

### Authorization options

 - `-a` or `--auth` USERNAME:PASSWORD
 - `-o` or `--oauth-token` TOKEN

### Enterprise options

 - `-u` or `--url` URL

## Examples

### Simple command line usage

    $ java -jar target/github-companion-0.1.0-SNAPSHOT.jar --url https://my.example.org/api/v3/ --oauth-token 0123456789abcdef0123456789abcdef01234567 grant org/team-slurp
    
### Properties file

You can also set a properties file with the default options in your home directory `.github-companion.properties`:

```data
oauth-token=0123456789abcdef0123456789abcdef01234567
url=https://my.example.org/api/v3/
```

Then you could simply run:

    $ java -jar target/github-companion-0.1.0-SNAPSHOT.jar grant org/team-slurp
    $ java -jar target/github-companion-0.1.0-SNAPSHOT.jar list-teams org
    $ java -jar target/github-companion-0.1.0-SNAPSHOT.jar protect org/repo
    $ java -jar target/github-companion-0.1.0-SNAPSHOT.jar protect-team org/team-slurp

## License

Copyright © 2016 Whitepages Inc.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
