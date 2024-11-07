# Getting started with TinyWasmR Engine
Engine itself is not useful without ability to load WebAssmbly module, so we are going to include `parser` component in order to parse from binary.

## Baking the module
To make a new binary module, you need the following ingredient:

- 1x WebAssembly Text module (file extension is usually `.wat` or `.wast`, but it can be anything really)

You also need the following cooking tool:

- `wat2wasm` (part of [WebAssembly Binary Toolkit][wabt] kitchen set).

### Buying the WebAssembly Text module
Since not many people have WebAssembly Text module in house, we are going to buy them from the nearest programmer's store. And since programmer's store only accept characters as currency, we are going to provide them the following text:

```webassembly
(module
    (func (export "main") (result i32)
        i32.const 42
    )
)
```

### Steal from the author of this recipe
If you so poor you can't purchase by typing characters, you can steal one from @nahkd123 by opening [this container](../../parser/src/test/resources/binary) (not guaranteed to be compatible with this recipe, but go on I guess).

### Actually baking
Now that we have a new and fresh text module, it's time to bake it into WebAssembly Binary module (usually with file extension `.wasm` but you can just ignore it, not like it's going to make your PC explode or anything).

Open the oven, put your text module in, set the timer to `wat2wasm path/to/module.wat path/to/module.wasm`, close the door and wait for 10 milliseconds.

```console
$ time wat2wasm module.wat -o module.wasm
real    0m0.001s
user    0m0.000s
sys     0m0.000s
$ ls
module.wat  module.wasm
```

There you go! We just baked a new WebAssembly Binary module.

## Consuming the module
Legends said that one must consume WebAssembly module with utmost respect, which means we have to follow a set of guidelines to ensure you are doing it right.

### Abusing quantum mechanic to replicate the module
Since you are just getting started, we need a test run to ensure that you are not screwing everything up, which is why we need to replicate the module using [DUKE][duke]-42 quantum machine:

```java
var stream = new FileInputStream("path/to/module.wasm");
var parser = new BinaryModuleParser(new SystemLogger(false), true);
var module = parser.parseModule(stream); // <= replicated module

// Cut off quantum tunnel to prevent contamination
stream.close();
```

A nice thing about this replication technique is that you can do as many times as you want, but the replicated modules will disappears when you cut off power from [DUKE][duke]-42.

### Taking a piece of (replicated) module
Now that we have a new replicated module, it's time to take a piece from it.

```java
var instance = new DefaultInstance(module); // a piece of module
System.out.println(instance.export("main").asFunction().exec()); // => 42
```

Is it good? Pretty use the taste is just as good as the answer to universe and everything.

[wabt]: https://github.com/WebAssembly/wabt
[duke]: https://dev.java/duke/