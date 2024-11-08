# How virtual machine in TinyWasmR works?
## Table of Contents
- [Terminologies](#terminologies)
- [Frame](#frame)
    + [All frames](#all-frames)
    + [External](#external-frame)
    + [Funtion](#function-frame)
    + [Block](#block-frame)
- [Branching](#branching)
    + [How `br` works](#how-br-works)
    + [Block with results](#block-with-results)
    + [Branching and frame stack](#branching-and-frame-stack)
    + [Returning](#returning)

## Terminologies
- Insn(s) - Instruction(s)
- Immediate - a constant value that will never be changed, usually obtained while decoding instruction (for example, if the instruction is `i32.const 42` then the immediate is `42`).

## Frame
Each frame represent a scope of execution. There are 3 kinds of frame: external frame, function frame and block frame.

### All frames
All kinds of frame have these things in common:

- Operand stack: to hold the values for insns;
- Insn pointer: to point towards the current executing insn, as well as determining whether to pop the frame after one step of execution.

### External frame
This is the first frame on the virtual machine's frame stack. The purpose of this frame is to collect the values returned from the first executed function, because when you exit a function, it will push the results to previous frame, and there must be something to collect them.

Values in external frame will only be pushed by WebAssembly code and popped by embedder, like WebAssembly function return values and Java application collecting returned values and map them as Java objects for example.

### Function frame
This if the first frame after the call to execute the function. When you call a function, the executor will push function frame with function reference and initialized locals to machine's frame stack. If you use TinyWasmR CLI debugger, you will see the following output when you use `inspect vm` after calling the function (and add a break right at function's head):

```
(dbg) inspect vm
Inspection: Virtual machine
  Not trapped
  No runtime validation
  Frames:
    0: External Frame
    1: Function: modulename::main()  insn 0000 i32.const 42
```

If you call a function while another function is being executed, it will also push the function frame to the frame stack:


```
(dbg) call modulename::test()
Manually called modulename::test() -> i32
Warning: modulename::test() will push (i32) to the stack after execution.

(dbg) inspect vm
Inspection: Virtual machine
  Not trapped
  No runtime validation
  Frames:
    0: External Frame
    1: Function: modulename::main() -> void   insn 0000 i32.const 42
    2: Function: modulename::test() -> (i32)  insn 0000 loop -> void
       local i32: 0
```

### Block frame
When entering a block, a new block frame is pushed to the frame stack. And because all frame have its own operand stack, it is also not possible for code inside the block to accesss the operand stack of its parent.

```
(dbg) inspect vm
Inspection: Virtual machine
  Not trapped
  No runtime validation
  Frames:
    0: External Frame
    1: Function: modulename::main() -> void  insn 0008 block -> void
       operand i32: 123
       operand i32: 51
    2: Block                                 insn 0001 i32.const 12
       operand i32: 12
```

In this case, we entered a block with `void` result and we are currently at 2nd insn in the block. If you try to `drop` 2 times, it will trap the virtual machine, because our block only have 1 operand on the stack. To access anything outside its scope, use local variables.

## Branching
### How `br` works?
This one family of insn took me more than a year to figure out, simply because the specification was written in such a way an average people can't understand.

The `N` immediate from `br N` is the index of indentation, but instead of indexing from outer to inner, it index in reverse order of it. For example, let's say you have the following assembly:

```webassembly
(block ;; 1
    (block ;; 2
        (block ;; 3
            (block ;; 4
                br 1
            )
        )
        ;; HERE!
    )
)
```

The immediate `1` in `br 1` point towards the block with `;; 3` comment next to it, which means after `br 1`, the insn pointer will move to `;; HERE!` and resume the execution from there.

### Block with results
Let's say you modified the above assembly to include the block results like this:

```webassembly
(block (result i32 i32 i32) ;; 1
    i32.const 1
    (block (result i32 i32) ;; 2
        i32.const 2
        (block (result i32) ;; 3
            i32.const 3
            (block          ;; 4
                i32.const 4
                i32.const 5
                i32.const 6
                br 2
            )
        )
    )
    ;; HERE!
)
```

The execution of `br 2` will do the following:

1. Find the block with index `N`, starting from current (most inner) block to outer block.
    + The block that we selected is `;; 2`
2. Determine the values to pop from current stack
    + We are still inside `;; 4`, so the current stack is `[4, 5, 6]`
    + Block `;; 2` have `(i32 i32)` results, so we need to pop 2 values
3. Collect the results
    + The results that we just collected are `[5, 6]`, leaving `[4]` on the current stack
4. Branch to the end of block with index `N`
    + We basically exited the block `;; 2` here
5. Push the results to the current stack
    + We are now in `;; 1`, and the current operand stack is `[1]`
    + After pushing, the operand stack will be `[1, 4, 5]`

After the execution of the above assembly, we will get `[1, 4, 5]` on the stack.

### Branching and frame stack
In TinyWasmR, the `N` of `br N` points to the frame at index `N`, starting from the top of the stack (so if the frame stack have 5 frames and `N` is 1, it will select the 4th frame). It then collect the results from current operand stack, pop until it popped either the target frame _or_ the function frame and push the results to new current operand stack.

### Returning
Returning works the same way as branching, but replace `N` with the index of the function frame, starting from the top of the frame.