# TinyWasmR
_Tiny WebAssembly runner/engine_

## Warning: Heavily in development!
It's not ready for production use yet; please come back later.

## Motivation
_Why was it created in the first place?_

I wanted to load and play some WASM4 games on Minecraft ([Minicraft](https://wasm4.org/play/minicraft/) to be precise). Now while there is at least 1 WASM engine written in Java, I wanted to try to take on the challenge of making my own WebAssembly virtual machine, mainly for fun.

Let's be honest here: TinyWasmR initial goal wasn't "bringing component written in C++/Rust to Java" (there's something more efficient and it's called JNI). The goal was to create a virtual machine where I can save or load its states whenever I wanted, which means it's not going to be performant.

## Alternatives
- [Wasmer Java Runtime](https://github.com/wasmerio/wasmer-java)
- Compile your component and use it with JNI instead.

## TinyWasmR components
- `engine`: The engine part (a.k.a the virtual machine). Contains `.wasm` parsing and virtual machine.
- `wasm4-fabric`: The Fabric mod that adds "WASM-4 Handheld Console" to Minecraft, along with WASM-4 runtime. Not available yet because the engine is not finished lol.

## License
MIT License

TODO add MIT license header to all source files
