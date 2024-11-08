# Modules, instances and runtime objects
In TinyWasmR, there are 2 different thing when it comes to running WebAssembly code: module and instance.

## Table of Contents
- [Module](#module)
    + [Declarations](#declarations)
- [Instance](#instance)
- [Runtime objects](#runtime-objects)
- [Function](#function)
    + [Instanced function](#instanced-function)
    + [Instanceless function](#instanceless-function)

## Module
Think of a module as your typical Java class, where you can define the methods and fields for your object. Assuming your class does not have anything static, in order to use your newly created class, you need to make an _instance_ of it (by using `new` keyword). In TinyWasmR, in order to use your module that you, for example, just parsed from a file, you need to make an `Instance` based on that module.

### Declarations
Declarations like `FunctionDecl` or `MemoryDecl` only contains the instruction on how to obtain `Function` or `Memory` from `Instance`. Think of these as unique ID, where each ID is tied to a memory, a table, a global variable or a function in `Instance`, except they are not being represented as unique ID in TinyWasmR.

- `Module*Decl` instructs the instance how to initialize the runtime object.
- `Import*Decl` instructs the instance which runtime object to import from `Importer`.
- `Extern*Decl` instructs the instance how to interact with host.
- Anything else will raise an exception.

## Instance
There can be more than 1 instance linking to the same module, each with its own set of states, like global variables, memories and table values. Module, on the other hand, only defines what the instance can have, as well as the code to manipulate the states of virtual machine and instance. This is why after you parsed your module into `WasmModule`, you can only get objects like `FunctionDecl` or `MemoryDecl` from it.

While module have declarations, instance have runtime objects. Those runtime objects are `Function`, `Memory`, `Table` and `Global`, and you can interact with them, such as setting `u32` inside the instance's main memory.

## Runtime objects
Runtime objects are created from the instantiation of module. An instance contains a collection of runtime objects, which can be considered as states of the instance. Instanced function is the only runtime object that links to instance, while others like `Memory` and `Table` may not have declaration (which usually indicates that they are created manually).

## Function
### Instanced function
Each `Function` created from instantiation are tied to a specific instance. When you execute the function, it will directly affect the instance that linked to it.

### Instanceless function
`Function` that does not have instance is called "instanceless function". Function like that usually tied to `ExternalFunctionDecl`, where instance may or may not required to successfully execute it. Instanceless function is not available for functions declared in module, however.