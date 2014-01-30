# open-descriptors

A JSON API for calculating open source molecular descriptors. Currently exposes a subset of the molecular descriptors in the [Chemistry Development Kit](https://github.com/cdk/cdk).

## Example Usage

Some examples using curl, presuming the service is running locally on port 3000.

GET a list of available descriptors:
    
    curl http://localhost:3000

Calculate the descriptors for a Molfile named "caffeine.mol":

    curl --form "molfile=<caffeine.mol" http://localhost:3000

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

## License

See LICENSE.txt
