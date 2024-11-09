(module
  (global $answer (export "answer") (mut i32) (i32.const 42))
  (func (export "main") (result i32)
    global.get $answer
    (global.set $answer (i32.const 727))
  )
)