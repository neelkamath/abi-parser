# ABI Parser

This project was built for the Starknet Hacker House on 2023-08. It parses a Cairo 2 ABI. It was supposed to generate an OpenAPI spec from it so that OpenAPI Generator could be used to build an API wrapper library for any language. However, I was too lazy to finish it.

## Installation

1. Install [JVM](https://adoptopenjdk.net/releases.html).
2. Clone the repo using one of the following methods:
    - SSH:
   
        ```
        git clone git@github.com:neelkamath/abi-parser.git
        ```
    - HTTPS:
   
        ```
        git clone https://github.com/neelkamath/abi-parser.git
        ```

## Usage

- Windows:

    ```
    gradlew run
    ```
- Others:

    ```
    ./gradlew run
    ```

## License

This project is under the [MIT License](LICENSE).
