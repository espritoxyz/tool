Symbolic analyzer for TVM (The Telegram Open Network Virtual Machine) based on USVM.

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
