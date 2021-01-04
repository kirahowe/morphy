<p align="center">
  <a href="https://github.com/kiramclean/morphy/actions?query=workflow%3ATests">
    <img src="https://github.com/kiramclean/morphy/workflows/Tests/badge.svg" alt="Morphy Clojure CI on Github Actions" >
  </a>

  <a href="https://app.circleci.com/pipelines/github/kiramclean/morphy">
    <img src="https://circleci.com/gh/kiramclean/morphy.svg?style=shield" alt="Morphy static site generator circleci status" >
  </a>

  <a href="https://codecov.io/gh/kiramclean/morphy">
    <img src="https://codecov.io/gh/kiramclean/morphy/branch/main/graph/badge.svg?token=SMXPW0BF1Q" alt="Morphy static site generator code coverage status" >
  </a>
</p>

# morphy.core

This is WIP. Not advisable to try to use it for anything yet.. everything will change!

FIXME: my new application.

## Installation

Download from https://github.com/morphy/morphy.core.

## Usage

- look for markdown posts in resources/content
  - anything that is markdown will get converted to html
  - anything that is not markdown will get copied as-is
  - the directory structure is preserved
  - any loose files get put in a directory and named `index.html` so you don't
    need the `.html` suffix to view them
  - naming pages priority:
    1. slug in markdown metadata
    2. slugified title in markdown metadata
    3. slugified file name

FIXME: explanation

Run the project directly:

    $ clojure -m morphy.core

Run the project's tests:

    $ clj -M:test

Run the project's tests and report code coverage:

    $ clj -M:coverage

Build an uberjar:

    $ clojure -A:uberjar

Run that uberjar:

    $ java -jar morphy.core.jar

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2020 Kira McLean

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
