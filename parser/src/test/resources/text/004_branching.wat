(module
  (func (export "main") (result i32 i32 i32)
    i32.const 1
    (block $outer (result i32)
      (block $inner (result i32 i32)
        i32.const 2
        i32.const 3
        br $outer
      )
      drop
    )
    i32.const 4
  )
)