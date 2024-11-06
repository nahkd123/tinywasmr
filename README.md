# TinyWasmR
_Tiny (?) WebAssembly runner/engine_

## Warning: Heavily in development!
It's not ready for production use yet; please come back later.

## Motivation
_Why was it created in the first place?_

I wanted to load and play some WASM4 games on Minecraft ([Minicraft](https://wasm4.org/play/minicraft/) to be precise). Now while there is at least 1 WASM engine written in Java, I wanted to try to take on the challenge of making my own WebAssembly virtual machine, mainly for fun.

Let's be honest here: TinyWasmR initial goal wasn't "bringing component written in C++/Rust to Java" (there's something more efficient and it's called JNI). The goal was to create a virtual machine where I can save or load its states whenever I wanted, which means it's not going to be performant.

### Goals
- Deep access into WebAssembly VM - so deep you can even alternate the branching.
- Purely in Java - I want to run this in Minecraft so I have to use Java.
- Correctly implement the WebAssembly specification.

### Non-goals
- Fastest WebAssembly VM - [No need to be faster than blazing fast][alt-wasmer].

### How about [Chicory][alt-chicory]?
I personally think Chicory is a nice library for running your WebAssembly module without having to touch native code. While Chicory and this TinyWasmR shares the same non-goals, one of TinyWasmR goals is to let you dive deep into the states of virtual machine, something Chicory is not trying to do.

One reason for getting inside virtual machine is to save the states into a file (a.k.a making snapshot of the VM), allowing you to resume the execution from those saved states.

## Alternatives
- Google's v8 engine
- [Chicory][alt-chicory] Purely in Java!
- [Wasmer Java Runtime][alt-wasmer] - Best one in town if you want to run WebAsembly without caring about all nerdy things.
- Compile your component and use it with JNI instead.

## TinyWasmR components
- `engine`: The engine part (a.k.a the virtual machine). Contains virtual machine.
- `parser`: WebAssembly binary and text parser. Parses your module to `WasmModule`.
- `probe`: WebAssembly module probing tool. Basically like disassembler, but with different syntax.
- `trace`: WebAssembly execution tracing tool (also known as debugging tool). Trace the execution of the function, like watching the frames stack, operands stack, locals, tables and memories for example. You can even mess around with them while the module is running.

## Future
- `jit`: TinyWasmR Just-in-time compiler - compiles module function into Java bytecode for performance. Note that you can't step each individual instruction once the function is JIT'd. I imagine the main use case for this is running WebAssembly modules on environment where native libraries can't be loaded by JVM.
- `state`: WebAssembly virtual machine states serializer and deserializer. You might want to use this to save the states during debugging session.

## License
MIT License

TODO add MIT license header to all source files

[alt-chicory]: https://github.com/dylibso/chicory
[alt-wasmer]: https://github.com/wasmerio/wasmer-java