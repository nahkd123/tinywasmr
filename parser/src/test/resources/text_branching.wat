(module
  (import "sys" "print" (func $print (param i32)))
  (func (export "branching") (param i32 i32) (result i32)
    (block $b01
      (block $b02
        i32.const 69
        return
        i32.const 1
        call $print)
      i32.const 2
      call $print)
    i32.const 3
    call $print
    i32.const 1))
