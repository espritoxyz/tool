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
4. Build the project, running `./gradlew build` from the root of the repo.