---
layout: default
title: Errors detection and tests generation mode
parent: Getting started
nav_order: 1
---

As a static analyzer, `TSA` can operate in two modes: runtime error detection for local smart contracts with report generation in [SARIF format](https://sarifweb.azurewebsites.net/) or test generation for [Blueprint](https://github.com/ton-org/blueprint) projects.
For operating in this mode, use `tsa-cli.jar` or corresponding options in the Docker Container.

## Runtime Error Detection

In runtime error detection mode, `TSA` accepts as input a contract file in one of the following formats: Tact (experimental) or FunC source code, or Fift assembler code, or BoC (compiled code). Optionally, it also accepts a [TL-B scheme](https://docs.ton.org/v3/documentation/data-formats/tlb/tl-b-language) for the `recv_internal` method (about TL-B schemes importance check [the internal design-document](../design/tlb.md)). For detailed input format information, use the `--help` argument. 

The output in this mode is a SARIF report containing the following information about methods that may encounter a [TVM error](https://docs.ton.org/v3/documentation/tvm/tvm-exit-codes) during execution:

- Instruction coverage percentage by the analyzer for the method
- Method number and TVM bytecode instruction where the error may occur
- Error code and type
- Call stack (method number - instruction)
- Possible (but not necessarily unique) parameter set causing the error
- Approximate gas usage up to the error

For more information about error types, see the [relevant section](../error-types.md).

### Examples

Consider a simple smart contract that may encounter a cell overflow error when the `write` method receives a value greater than 4:

```c
(builder) write(int loop_count) method_id {
    builder b = begin_cell();

    if (loop_count < 0) {
        return b;
    }

    var i = 0;
    repeat(loop_count) {
        builder value = begin_cell().store_int(i, 32);

        b = b.store_ref(value.end_cell());
    }

    return b;
}

() recv_internal(int msg_value, cell in_msg, slice in_msg_body) impure {
    ;; Do nothing
}
```

The analyzer's output for this contract will identify the error in the following format:

```json
{
    "$schema": "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
    "version": "2.1.0",
    "runs": [
        {
            "properties": {
                "coverage": {
                    "0": 100.0,
                    "75819": 100.0
                }
            },
            "results": [
                {
                    "codeFlows": [
                        {
                            "threadFlows": [
                                {
                                    "locations": [
                                        {
                                            "location": {
                                                "logicalLocations": [
                                                    {
                                                        "decoratedName": "75819",
                                                        "properties": {
                                                            "stmt": "REPEAT#8"
                                                        }
                                                    }
                                                ]
                                            }
                                        }
                                    ]
                                }
                            ]
                        }
                    ],
                    "level": "error",
                    "locations": [
                        {
                            "logicalLocations": [
                                {
                                    "decoratedName": "75819"
                                }
                            ]
                        }
                    ],
                    "message": {
                        "text": "TvmFailure(exit=TVM integer out of expected range, exit code: 5, type=UnknownError)"
                    },
                    "properties": {
                        "gasUsage": 220,
                        "usedParameters": [
                            "2147483648"
                        ],
                        "resultStack": [
                            "0"
                        ]
                    },
                    "ruleId": "integer-out-of-range"
                }
            ],
            "tool": {
                "driver": {
                    "name": "TSA",
                    "organization": "Explyt"
                }
            }
        }
    ]
}
```

For more examples containing erroneous places, take a look at the directory in [the repository with manually written contracts](https://github.com/espritoxyz/tsa/tree/master/tsa-core/src/test/resources).
Feel free to run TSA by yourself for these contracts or consider [tests for them](https://github.com/espritoxyz/tsa/tree/master/tsa-core/src/test/kotlin/org/ton/examples). 

## Test Generation

In test generation mode, `TSA` takes as input a project in the [Blueprint](https://github.com/ton-org/blueprint) format and 
the relative path to the source code of the analyzed contract (as before, use `--help` argument for more detailed information about input format).

In this mode, `TSA` generates a corresponding `wrapper` in `Typescript` under the `wrappers` directory and a test file for the contract in the `tests` directory. The test file contains regression tests for execution branches of methods that terminate with a TVM error (for more information about error types, see the [relevant section](../error-types.md)).

### Examples

For the [wallet-v4](https://github.com/ton-blockchain/wallet-contract) contract, a test file will be generated with tests similar to the following:

```ts
import {Blockchain} from '@ton/sandbox'
import {Address, beginCell, Builder, Cell, Dictionary, DictionaryValue, Slice} from '@ton/core'
import '@ton/test-utils'
import {compileFunc} from "@ton-community/func-js"
import * as fs from "node:fs"
import {WalletV4Code} from "../wrappers/WalletV4Code"

async function compileContract(): Promise<Cell> {
    let compileResult = await compileFunc({
        targets: ['contracts/wallet-v4-code.fc'],
        sources: (x) => fs.readFileSync(x).toString("utf8"),
    })

    if (compileResult.status === "error") {
        console.error("Compilation Error!")
        console.error(`\n${compileResult.message}`)
        process.exit(1)
    }

    return Cell.fromBoc(Buffer.from(compileResult.codeBoc, "base64"))[0]
}

const sliceValue: DictionaryValue<Slice> = {
    serialize: (src: Slice, builder: Builder) => {
        builder.storeSlice(src)
    },
    parse: (src: Slice) => {
        return src.clone();
    }
}

describe('TvmTest', () => {
    let code: Cell
    let blockchain: Blockchain

    beforeAll(async () => {
        code = await compileContract()
    })

    beforeEach(async () => {
        blockchain = await Blockchain.create()
    })

    it('test-0', async () => {
        const data = beginCell().storeUint(BigInt("0b0"), 173).endCell()
        const msgBody = beginCell().endCell()
        const from = Address.parseRaw("0:0000000000000000000000000000000000000000000000000000000000000000")
        const bounce = false
        const bounced = false

        const contractAddress = Address.parseRaw("0:0000000000000000000000000000000000000000000000000000000000000000")
        const contract = blockchain.openContract(new WalletV4Code(contractAddress, { code, data }))
        await contract.initializeContract(blockchain, 10000000n)
  
        const sentMessageResult = await contract.internal(
            blockchain,
            from,
            msgBody,
            10000000n,
            bounce,
            bounced
        )
        expect(sentMessageResult.transactions).toHaveTransaction({
            from: from,
            to: contractAddress,
            exitCode: 9,
        })
    })
})
```
