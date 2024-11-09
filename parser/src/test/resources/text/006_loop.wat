(module
  (import "console" "log" (func $log (param i32)))
  (func (export "main") (local i32)
    (local.set 0 (i32.const 0))
    (loop
      (call $log (local.get 0))
      (local.set 0 (i32.add (local.get 0) (i32.const 1)))
      (br_if 0 (i32.lt_s (local.get 0) (i32.const 10)))
    )
  )
)