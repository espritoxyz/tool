Symbolic analyzer for TVM (The Telegram Open Network Virtual Machine) based on USVM.

## How to run

### Linux/MacOS

#### Using built Docker image

```bash
docker run --platform linux/amd64 -it --rm -v [SOURCES_DIR_ABSOLUTE_PATH]:/project ghcr.io/espritoxyz/tsa:latest [ANALYZER_OPTIONS]
```
, where:

- `SOURCES_DIR_ABSOLUTE_PATH` is an absolute path to the dir where the analyzed sources are located;
- `ANALYZER_OPTIONS` consists of a target language and corresponding options (for more details, use `--help` as provided options);

**NOTE**: all paths in `ANALYZER_OPTIONS` MUST be relative to the `SOURCES_DIR_ABSOLUTE_PATH`.

For example, to analyze inter-contract communication between two contracts (with FunC sources provided), you can run:

```bash
docker run --platform linux/amd64 -it --rm -v [SOURCES_DIR_ABSOLUTE_PATH]:/project ghcr.io/espritoxyz/tsa:latest inter |
/project/[FIRST_CONTRACT_RELATIVE_PATH] /project/[SECOND_CONTRACT_RELATIVE_PATH] --func-std /project/[PATH_TO_FUNC_STDLIB] --fift-std /project/[PATH_TO_FIFT_STDLIB_DIR]
```

#### Using JAR executables

There are two JAR executables available at the [Releases page](https://github.com/espritoxyz/tsa/releases): 
- `tsa-cli.jar`
- `tsa-safety-properties.jar`

To use them, ensure you have `Java`, `fift`, `func`, and `tact` installed within your $PATH.

Then, run common analysis using 

```bash
java -jar tsa-cli.jar
```

or safety-properties checker using 

```bash
java -jar tsa-safety-properties.jar
```

### Windows

For now, TSA could be run on Windows only using JAR executables, see [the corresponding section](#using-jar-executables)

## How to build

1. Clone this repo with all submodules (using `IntelliJ Idea` or `git clone git clone --recurse-submodules https://github.com/explyt/tsa`).
2. Build the submodule:

    ```bash
    cd tvm-disasm
    npm i
    npm run build
    ```
3. Download `fift` and `func` for the corresponding operating system from [the last TON release ](https://github.com/ton-blockchain/ton/releases/) and add them to `$PATH`.
    - rename the downloaded files (e.g., `func-mac-arm64`, `fift-mac-arm64`)  to `fift` and `func`.
    - if you have `Permission denied` error, do `chmod +x fift` and `chmod +x func`
    - on MacOS you need to manually open these files once
4. Install `tact` compiler with [yarn](https://classic.yarnpkg.com/lang/en/docs/install):
   ```bash
   yarn global add @tact-lang/compiler
   ```
   - probably, you need to manually add `tact` to path:
       ```bash
       export PATH="$PATH:$(yarn global bin)"
       ```
5. Build the project, running `./gradlew build` from the root of the repo.
