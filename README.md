# TinyWasmR
_Tiny (?) WebAssembly runner/engine_

## Warning: Heavily in development!
It's not ready for production use yet; please come back later.

## Links
- [Documentations][docs]
	+ [Engine](./docs/engine/index.md)
	+ Parser (n/a)
	+ Externals helper (n/a)
	+ Debugger (n/a)
	+ WASM-4 fantasy console runner (n/a)

## Project status
TinyWasmR can now parse and run [Watris](https://wasm4.org/play/watris/)! While it doesn't output things correctly (because `w4` is still in progress), the `engine` and `parser` components can now read and understand a single [WASM-4][w4] game, which is a huge milestone personally.

TinyWasmR still lacking other stuffs:

- [ ] Table element initialization
- [ ] Start function
- [ ] Fully implement WASM-4 (missing font atm)
- [ ] Pass all tests

## Usage
### Simplest usage
```java
// Parse from binary module
InputStream stream = new FileInputStream("path/to/cool_module.wasm");
WasmModule module = BinaryModuleParser.parse(stream);

// Using our module
// We are not importing functions to the module for the time being, so the 2nd param is null.
// Instance holds the global variables, memories and tables. Each instance have a different set
// of globals, memories and tables (so are their contents).
Instance instance = new DefaultInstance(module, null);
System.out.println(instance.export("addTwo").asFunction().exec(1, 2)); // => 3
```

That's a lot of noises. Let's remove all comments and witness the tiniest amount of code required to use WebAssembly module:

```java
WasmModule module = BinaryModuleParser.parse(new FileInputStream("path/to/cool_module.wasm"));
Instance instance = new DefaultInstance(module, null);
System.out.println(instance.export("addTwo").asFunction().exec(1, 2)); // => 3
```

Well, that's dead simple. While it is simple and can do the job quick, it is also limiting, because you can't see what it is doing in the black box called `exec()`. In the next section, I will show you a fun little trick to play around with virtual machine.

Here's the WebAssembly Text code for `cool_module.wast`. Convert that to `.wasm` using [WebAssembly Binary Toolkit][wabt] (in the future, TinyWasmR will be able to parse WebAssembly Text module directly).

```webassembly
(module
  (func (export "addTwo") (param i32 i32) (result i32)
    local.get 0
    local.get 1
    i32.add
  )
)
```

### Digging into execution flow
But what if you want to mess around with the program? Well, with TinyWasmR, you can!

```java
// Let's step the function one by one
Machine vm = new DefaultMachine();
Executor exec = new DefaultExecutor();

// This marker frame will be used to determine when we should stop stepping
// By default, the virtual machine will always have 1 frame that can't be popped.
Frame marker = vm.peekFrame();

// First, we need to "call" the function
Function addTwo = instance.export("addTwo").asFunction();
vm.pushFrame(FunctionFrame.createCall(addTwo, new Value[] { Value.mapFromJava(1), Value.mapFromJava(2) }));
// The above call will:
// 1. Create a new function frame
// 2. Set local #0 to 1 and local #1 to 2
// 3. Push the function frame to frame stack in virtual machine

// Now we step each instruction
// When the current frame is our marked frame, we know that is when to stop
while (vm.peekFrame() != marker) {
	// We are now stepping each individual instruction
	// Here, you can, for example, counting how many instructions has been executed
	// Or you can purposely change the flow of the program by manipulating the stack
	// yourself.
	// if (vm.peekFrame().getInsnIndex() == 727) {
	//     vm.peekFrame().popOperand();
	//     vm.peekFrame().pushOperand(Value.mapFromJava(Constants.WYSI));
	// }

	// Step by 1 instruction
	StepResult result = exec.step(vm);

	if (result == StepResult.TRAP) {
		// Our VM is trapped. Normally, you can't continue the execution when the VM is trapped.
		// However, you can un-trap the VM by... removing the trap.
		// Preferably we should throw an exception here and tell user to fix their module.
		vm.setTrap(null);
		continue;
	}
}
```

With great power comes great responsibility. I hope you are not going to abuse this to circumvent the protection of certain program, assuming that program was compiled into WebAssembly (like Blazor .NET for example). Maybe you can use the `trace` tool to play capture the flag? Who knows.

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
- `extern`: External helper component. Make your life easier with reflection tricks. May not work well with AoT (Ahead-of-time) compilers (which is why it is separated from `engine`).
- `w4`: WASM-4 implementation in Java. Currently in progress.

## Future
- `jit`: TinyWasmR Just-in-time compiler - compiles module function into Java bytecode for performance. Note that you can't step each individual instruction once the function is JIT'd. I imagine the main use case for this is running WebAssembly modules on environment where native libraries can't be loaded by JVM.
- `state`: WebAssembly virtual machine states serializer and deserializer. You might want to use this to save the states during debugging session.

## License and acknowledgements
- TinyWasmR (this repo) is licensed under [MIT License](./LICENSE)
- Part of [WASM-4][w4] by aduros (licensed under ISC License) has been ported to this project (in `w4` component)

TODO add MIT license header to all source files

[docs]: ./docs/index.md
[wabt]: https://github.com/WebAssembly/wabt
[w4]: https://github.com/aduros/wasm4
[alt-chicory]: https://github.com/dylibso/chicory
[alt-wasmer]: https://github.com/wasmerio/wasmer-java