# Standard Clojure Style in Java

This is a port of [Standard Clojure Style] in Java ☕

[Standard Clojure Style]:https://github.com/oakmac/standard-clojure-style-js

## Project Status (Aug 2025)

I am busy raising my young kids and this project is not in focus for me at the moment.

All of the parser test cases are passing, so a nice chunk of work is finished. But this project is not usable for formatting Clojure code in it's current state.

## Development

Make sure [java] and [gradle] are installed.

```sh
## Run unit tests
./gradlew test
./gradlew test --info

## build the project
./gradlew build

## clean the build
./gradlew clean

## format files
./gradlew googleJavaFormat
```

[java]:https://openjdk.org/
[gradle]:https://gradle.org/

## License

[ISC License](LICENSE.md)
