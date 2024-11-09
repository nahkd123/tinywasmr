(module
  (memory $memory (export "memory") 1)
  (data $data "hello world")
  (func (export "main")
    (memory.init $data (i32.const 10) (i32.const 6) (i32.const 5))
  )
)