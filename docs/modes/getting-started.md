---
layout: default
title: Getting started
nav_order: 2
---

# Getting started

Before starting using TSA, ensure it is [installed](../installation.md) properly.

The primary purpose of TSA is to enhance the reliability of the TON blockchain and its smart contracts. 
To achieve this goal, TSA can analyze trustworthy contracts in an automated mode (without user involvement) to identify errors, 
or in a semi-automated mode (with user-defined safety properties) to check smart contracts for vulnerabilities and reliability.

## Runtime Errors

The automated mode of TSA focuses on detecting errors in TON smart contracts, 
related to code implementation and causing the TON Virtual Machine (TVM) to crash during contract execution.

TVM runtime errors in TON smart contracts often arise from improper handling of data – 
primitives (numbers) and complex structures (slices, builders, dictionaries). 
The occurrence of [such errors](../error-types.md) makes it impossible to complete transactions, such as transferring funds, buying, or selling tokens, etc. 
The [main mode of operation](error-detection-tests-generation-mode.md) of TSA is to detect and reproduce such errors.

## Safety Properties

Sometimes, errors in smart contracts are not related to runtime errors but to incorrect business logic – 
for example, the inability to transfer funds from a wallet under certain conditions. 
These issues are complex in nature but can often be expressed using safety properties. 
The TSA [Safety Properties](safety-properties-mode.md) mode assists users in verifying both the business logic and required invariants of their own smart contracts, 
as well as checking the reliability of third-party contracts.