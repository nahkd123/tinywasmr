# Intro

Welcome to TinyWasmR documentation! TinyWasmR is consists of following components:

- [Engine](./engine/index.md): This is the core component that contains virtual machine, executor and instruction definitions. Any new instruction must be added to this component first.
- Parser: Like the name suggests, the purpose of this component is to parse from either binary or text module to something that the engine can digest.
- Externals Helper: This optional component contains external helpers to aid importing external objects from Java to WebAssembly module.