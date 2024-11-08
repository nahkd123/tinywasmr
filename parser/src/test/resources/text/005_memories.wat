(module
  (memory (export "memory") 1)
  (func (export "main")
    (i32.store
      (i32.const 0)
      (i32.add
        (i32.load (i32.const 0))
        (i32.const 42)
      )
    )
  )
)