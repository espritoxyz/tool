# **TSA** - Symbolic analyzer for **TVM** (The Telegram Open Network Virtual Machine) based on [USVM](https://github.com/UnitTestBot/usvm).

## How to build

1. Clone this repo with all submodules (using `IntelliJ Idea` or `git clone --recurse-submodules https://github.com/explyt/tsa`).
2. Build the submodule:

    ```bash
    cd tvm-disasm
    npm i
    npm run build
    ```
3. Download `fift` and `func` for the corresponding operating system from [the last TON release ](https://github.com/ton-blockchain/ton/releases/) and add them to `$PATH`.
4. Install `tact` compiler with [yarn](https://classic.yarnpkg.com/lang/en/docs/install):
   ```bash
   yarn global add @tact-lang/compiler
   ```
5. Build the project, running `./gradlew build` from the root of the repo.

## How to use

The analyzer could be used in two ways - with a plugin for **IntelliJ Idea** and as a command-line tool. For more information,
see README in the modules for [the plugin](tsa-intellij/README.md) and for [the CLI](tsa-cli/README.md), correspondingly.

## How to test

Existing tests are located [in the test directory of the core module](tsa-core/src/test). They consist of two parts:

- source files with tested code written in Tact/FunC/Fift or already compiled TVM-bytecode in the Bag of Cells (BoC) format
located in [the resources dir](tsa-core/src/test/resources);
- Kotlin tests located in the [kotlin dir](tsa-core/src/test/kotlin) running the analyzer for the corresponding sources.

To run tests, execute the following command from the root of the repo:

`./gradlew tsa-core:test`
