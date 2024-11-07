# Intro

Welcome to TinyWasmR documentation! TinyWasmR is consists of following components:

- [Engine](./engine/index.md): This is the core component that contains virtual machine, executor and instruction definitions. Any new instruction must be added to this component first.
- Parser: Like the name suggests, the purpose of this component is to parse from either binary or text module to something that the engine can digest.
- Probe: This component converts everything into human-readable text, like converting `new ConstInsn(new NumberI32Value(42))` into `i32.const 42` for example. Also contains CLI tool to disassemble `WasmModule` into WebAssembly Text.
- Trace: This CLI tool allows you to load WebAssembly modules, run them, pause the execution, as well as manipulating the states of virtual machine for debugging or reverse engineering purpose. Also available as library that you can plug into your debugger.