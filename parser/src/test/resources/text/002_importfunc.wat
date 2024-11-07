(module
  (import "myModule" "printI32" (func $printNum (param i32)))
  (import "myModule" "answer" (func $getAnswer (result i32)))
  (func (export "main")
    call $getAnswer
    call $printNum
  )
)