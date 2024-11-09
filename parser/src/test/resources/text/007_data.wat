(module
  (memory $memory (export "memory") 1)
  (data $data "hello world")
  (data (offset (i32.const 0)) "hello")
  (func (export "main")
    (memory.init $data (i32.const 10) (i32.const 6) (i32.const 5))
  )
)