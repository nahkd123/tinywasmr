(assert_return (invoke "type-i32"))
(assert_return (invoke "type-i64"))
(assert_return (invoke "type-f32"))
(assert_return (invoke "type-f64"))

(assert_return (invoke "type-i32-value") (i32.const 1))
(assert_return (invoke "type-i64-value") (i64.const 2))
(assert_return (invoke "type-f32-value") (f32.const 3))
(assert_return (invoke "type-f64-value") (f64.const 4))

(assert_return (invoke "empty" (i32.const 0)) (i32.const 22))
(assert_return (invoke "empty" (i32.const 1)) (i32.const 22))
(assert_return (invoke "empty" (i32.const 11)) (i32.const 22))
(assert_return (invoke "empty" (i32.const -1)) (i32.const 22))
(assert_return (invoke "empty" (i32.const -100)) (i32.const 22))
(assert_return (invoke "empty" (i32.const 0xffffffff)) (i32.const 22))

(assert_return (invoke "empty-value" (i32.const 0)) (i32.const 33))
(assert_return (invoke "empty-value" (i32.const 1)) (i32.const 33))
(assert_return (invoke "empty-value" (i32.const 11)) (i32.const 33))
(assert_return (invoke "empty-value" (i32.const -1)) (i32.const 33))
(assert_return (invoke "empty-value" (i32.const -100)) (i32.const 33))
(assert_return (invoke "empty-value" (i32.const 0xffffffff)) (i32.const 33))

(assert_return (invoke "singleton" (i32.const 0)) (i32.const 22))
(assert_return (invoke "singleton" (i32.const 1)) (i32.const 20))
(assert_return (invoke "singleton" (i32.const 11)) (i32.const 20))
(assert_return (invoke "singleton" (i32.const -1)) (i32.const 20))
(assert_return (invoke "singleton" (i32.const -100)) (i32.const 20))
(assert_return (invoke "singleton" (i32.const 0xffffffff)) (i32.const 20))

(assert_return (invoke "singleton-value" (i32.const 0)) (i32.const 32))
(assert_return (invoke "singleton-value" (i32.const 1)) (i32.const 33))
(assert_return (invoke "singleton-value" (i32.const 11)) (i32.const 33))
(assert_return (invoke "singleton-value" (i32.const -1)) (i32.const 33))
(assert_return (invoke "singleton-value" (i32.const -100)) (i32.const 33))
(assert_return (invoke "singleton-value" (i32.const 0xffffffff)) (i32.const 33))

(assert_return (invoke "multiple" (i32.const 0)) (i32.const 103))
(assert_return (invoke "multiple" (i32.const 1)) (i32.const 102))
(assert_return (invoke "multiple" (i32.const 2)) (i32.const 101))
(assert_return (invoke "multiple" (i32.const 3)) (i32.const 100))
(assert_return (invoke "multiple" (i32.const 4)) (i32.const 104))
(assert_return (invoke "multiple" (i32.const 5)) (i32.const 104))
(assert_return (invoke "multiple" (i32.const 6)) (i32.const 104))
(assert_return (invoke "multiple" (i32.const 10)) (i32.const 104))
(assert_return (invoke "multiple" (i32.const -1)) (i32.const 104))
(assert_return (invoke "multiple" (i32.const 0xffffffff)) (i32.const 104))

(assert_return (invoke "multiple-value" (i32.const 0)) (i32.const 213))
(assert_return (invoke "multiple-value" (i32.const 1)) (i32.const 212))
(assert_return (invoke "multiple-value" (i32.const 2)) (i32.const 211))
(assert_return (invoke "multiple-value" (i32.const 3)) (i32.const 210))
(assert_return (invoke "multiple-value" (i32.const 4)) (i32.const 214))
(assert_return (invoke "multiple-value" (i32.const 5)) (i32.const 214))
(assert_return (invoke "multiple-value" (i32.const 6)) (i32.const 214))
(assert_return (invoke "multiple-value" (i32.const 10)) (i32.const 214))
(assert_return (invoke "multiple-value" (i32.const -1)) (i32.const 214))
(assert_return (invoke "multiple-value" (i32.const 0xffffffff)) (i32.const 214))

(assert_return (invoke "large" (i32.const 0)) (i32.const 0))
(assert_return (invoke "large" (i32.const 1)) (i32.const 1))
(assert_return (invoke "large" (i32.const 100)) (i32.const 0))
(assert_return (invoke "large" (i32.const 101)) (i32.const 1))
(assert_return (invoke "large" (i32.const 10000)) (i32.const 0))
(assert_return (invoke "large" (i32.const 10001)) (i32.const 1))
(assert_return (invoke "large" (i32.const 1000000)) (i32.const 1))
(assert_return (invoke "large" (i32.const 1000001)) (i32.const 1))

(assert_return (invoke "as-block-first"))
(assert_return (invoke "as-block-mid"))
(assert_return (invoke "as-block-last"))
(assert_return (invoke "as-block-value") (i32.const 2))

(assert_return (invoke "as-loop-first") (i32.const 3))
(assert_return (invoke "as-loop-mid") (i32.const 4))
(assert_return (invoke "as-loop-last") (i32.const 5))

(assert_return (invoke "as-br-value") (i32.const 9))

(assert_return (invoke "as-br_if-cond"))
(assert_return (invoke "as-br_if-value") (i32.const 8))
(assert_return (invoke "as-br_if-value-cond") (i32.const 9))

(assert_return (invoke "as-br_table-index"))
(assert_return (invoke "as-br_table-value") (i32.const 10))
(assert_return (invoke "as-br_table-value-index") (i32.const 11))

(assert_return (invoke "as-return-value") (i64.const 7))

(assert_return (invoke "as-if-cond") (i32.const 2))
(assert_return (invoke "as-if-then" (i32.const 1) (i32.const 6)) (i32.const 3))
(assert_return (invoke "as-if-then" (i32.const 0) (i32.const 6)) (i32.const 6))
(assert_return (invoke "as-if-else" (i32.const 0) (i32.const 6)) (i32.const 4))
(assert_return (invoke "as-if-else" (i32.const 1) (i32.const 6)) (i32.const 6))

(assert_return (invoke "as-select-first" (i32.const 0) (i32.const 6)) (i32.const 5))
(assert_return (invoke "as-select-first" (i32.const 1) (i32.const 6)) (i32.const 5))
(assert_return (invoke "as-select-second" (i32.const 0) (i32.const 6)) (i32.const 6))
(assert_return (invoke "as-select-second" (i32.const 1) (i32.const 6)) (i32.const 6))
(assert_return (invoke "as-select-cond") (i32.const 7))

(assert_return (invoke "as-call-first") (i32.const 12))
(assert_return (invoke "as-call-mid") (i32.const 13))
(assert_return (invoke "as-call-last") (i32.const 14))

(assert_return (invoke "as-call_indirect-first") (i32.const 20))
(assert_return (invoke "as-call_indirect-mid") (i32.const 21))
(assert_return (invoke "as-call_indirect-last") (i32.const 22))
(assert_return (invoke "as-call_indirect-func") (i32.const 23))

(assert_return (invoke "as-local.set-value") (i32.const 17))
(assert_return (invoke "as-local.tee-value") (i32.const 1))
(assert_return (invoke "as-global.set-value") (i32.const 1))

(assert_return (invoke "as-load-address") (f32.const 1.7))
(assert_return (invoke "as-loadN-address") (i64.const 30))

(assert_return (invoke "as-store-address") (i32.const 30))
(assert_return (invoke "as-store-value") (i32.const 31))
(assert_return (invoke "as-storeN-address") (i32.const 32))
(assert_return (invoke "as-storeN-value") (i32.const 33))

(assert_return (invoke "as-unary-operand") (f32.const 3.4))

(assert_return (invoke "as-binary-left") (i32.const 3))
(assert_return (invoke "as-binary-right") (i64.const 45))

(assert_return (invoke "as-test-operand") (i32.const 44))

(assert_return (invoke "as-compare-left") (i32.const 43))
(assert_return (invoke "as-compare-right") (i32.const 42))

(assert_return (invoke "as-convert-operand") (i32.const 41))

(assert_return (invoke "as-memory.grow-size") (i32.const 40))

(assert_return (invoke "nested-block-value" (i32.const 0)) (i32.const 19))
(assert_return (invoke "nested-block-value" (i32.const 1)) (i32.const 17))
(assert_return (invoke "nested-block-value" (i32.const 2)) (i32.const 16))
(assert_return (invoke "nested-block-value" (i32.const 10)) (i32.const 16))
(assert_return (invoke "nested-block-value" (i32.const -1)) (i32.const 16))
(assert_return (invoke "nested-block-value" (i32.const 100000)) (i32.const 16))

(assert_return (invoke "nested-br-value" (i32.const 0)) (i32.const 8))
(assert_return (invoke "nested-br-value" (i32.const 1)) (i32.const 9))
(assert_return (invoke "nested-br-value" (i32.const 2)) (i32.const 17))
(assert_return (invoke "nested-br-value" (i32.const 11)) (i32.const 17))
(assert_return (invoke "nested-br-value" (i32.const -4)) (i32.const 17))
(assert_return (invoke "nested-br-value" (i32.const 10213210)) (i32.const 17))

(assert_return (invoke "nested-br_if-value" (i32.const 0)) (i32.const 17))
(assert_return (invoke "nested-br_if-value" (i32.const 1)) (i32.const 9))
(assert_return (invoke "nested-br_if-value" (i32.const 2)) (i32.const 8))
(assert_return (invoke "nested-br_if-value" (i32.const 9)) (i32.const 8))
(assert_return (invoke "nested-br_if-value" (i32.const -9)) (i32.const 8))
(assert_return (invoke "nested-br_if-value" (i32.const 999999)) (i32.const 8))

(assert_return (invoke "nested-br_if-value-cond" (i32.const 0)) (i32.const 9))
(assert_return (invoke "nested-br_if-value-cond" (i32.const 1)) (i32.const 8))
(assert_return (invoke "nested-br_if-value-cond" (i32.const 2)) (i32.const 9))
(assert_return (invoke "nested-br_if-value-cond" (i32.const 3)) (i32.const 9))
(assert_return (invoke "nested-br_if-value-cond" (i32.const -1000000)) (i32.const 9))
(assert_return (invoke "nested-br_if-value-cond" (i32.const 9423975)) (i32.const 9))

(assert_return (invoke "nested-br_table-value" (i32.const 0)) (i32.const 17))
(assert_return (invoke "nested-br_table-value" (i32.const 1)) (i32.const 9))
(assert_return (invoke "nested-br_table-value" (i32.const 2)) (i32.const 8))
(assert_return (invoke "nested-br_table-value" (i32.const 9)) (i32.const 8))
(assert_return (invoke "nested-br_table-value" (i32.const -9)) (i32.const 8))
(assert_return (invoke "nested-br_table-value" (i32.const 999999)) (i32.const 8))

(assert_return (invoke "nested-br_table-value-index" (i32.const 0)) (i32.const 9))
(assert_return (invoke "nested-br_table-value-index" (i32.const 1)) (i32.const 8))
(assert_return (invoke "nested-br_table-value-index" (i32.const 2)) (i32.const 9))
(assert_return (invoke "nested-br_table-value-index" (i32.const 3)) (i32.const 9))
(assert_return (invoke "nested-br_table-value-index" (i32.const -1000000)) (i32.const 9))
(assert_return (invoke "nested-br_table-value-index" (i32.const 9423975)) (i32.const 9))

(assert_return (invoke "nested-br_table-loop-block" (i32.const 1)) (i32.const 3))

(assert_return (invoke "meet-externref" (i32.const 0) (ref.extern 1)) (ref.extern 1))
(assert_return (invoke "meet-externref" (i32.const 1) (ref.extern 1)) (ref.extern 1))
(assert_return (invoke "meet-externref" (i32.const 2) (ref.extern 1)) (ref.extern 1))

(assert_invalid
  (module (func $type-arg-void-vs-num (result i32)
    (block (br_table 0 (i32.const 1)) (i32.const 1))
  ))
  "type mismatch"
)

(assert_invalid
  (module (func $type-arg-empty-vs-num (result i32)
    (block (br_table 0) (i32.const 1))
  ))
  "type mismatch"
)

(assert_invalid
  (module (func $type-arg-void-vs-num (result i32)
    (block (result i32) (br_table 0 (nop) (i32.const 1)) (i32.const 1))
  ))
  "type mismatch"
)
(assert_invalid
  (module (func $type-arg-num-vs-num (result i32)
    (block (result i32)
      (br_table 0 0 0 (i64.const 1) (i32.const 1)) (i32.const 1)
    )
  ))
  "type mismatch"
)
(assert_invalid
  (module (func $type-arg-num-vs-arg-num
    (block
      (block (result f32)
        (br_table 0 1 (f32.const 0) (i32.const 0))
      )
      (drop)
    )
  ))
  "type mismatch"
)
(assert_invalid
  (module (func
    (block (result i32)
      (block (result i64)
        (br_table 0 1 (i32.const 0) (i32.const 0))
      )
    )
  ))
  "type mismatch"
)

(assert_invalid
  (module (func $type-index-void-vs-i32
    (block (br_table 0 0 0 (nop)))
  ))
  "type mismatch"
)
(assert_invalid
  (module (func $type-index-num-vs-i32
    (block (br_table 0 (i64.const 0)))
  ))
  "type mismatch"
)
(assert_invalid
  (module (func $type-arg-index-void-vs-i32 (result i32)
    (block (result i32) (br_table 0 0 (i32.const 0) (nop)) (i32.const 1))
  ))
  "type mismatch"
)
(assert_invalid
  (module (func $type-arg-void-vs-num-nested (result i32)
    (block (result i32) (i32.const 0) (block (br_table 1 (i32.const 0))))
  ))
  "type mismatch"
)
(assert_invalid
  (module (func $type-arg-index-num-vs-i32 (result i32)
    (block (result i32)
      (br_table 0 0 (i32.const 0) (i64.const 0)) (i32.const 1)
    )
  ))
  "type mismatch"
)

(assert_invalid
  (module (func $type-arg-void-vs-num (result i32)
    (block (br_table 0 (i32.const 1)) (i32.const 1))
  ))
  "type mismatch"
)

(assert_invalid
  (module
    (func $type-arg-index-empty-in-then
      (block
        (i32.const 0) (i32.const 0)
        (if (result i32) (then (br_table 0)))
      )
      (i32.eqz) (drop)
    )
  )
  "type mismatch"
)
(assert_invalid
  (module
    (func $type-arg-value-empty-in-then
      (block
        (i32.const 0) (i32.const 0)
        (if (result i32) (then (br_table 0 (i32.const 1))))
      )
      (i32.eqz) (drop)
    )
  )
  "type mismatch"
)
(assert_invalid
  (module
    (func $type-arg-index-empty-in-return
      (block (result i32)
        (return (br_table 0))
      )
      (i32.eqz) (drop)
    )
  )
  "type mismatch"
)
(assert_invalid
  (module
    (func $type-arg-value-empty-in-return
      (block (result i32)
        (return (br_table 0 (i32.const 1)))
      )
      (i32.eqz) (drop)
    )
  )
  "type mismatch"
)

(assert_invalid
  (module
    (func (param i32) (result i32)
      (loop (result i32)
        (block (result i32)
          (br_table 0 1 (i32.const 1) (local.get 0))
        )
      )
    )
  )
  "type mismatch"
)
(assert_invalid
  (module
    (func (param i32) (result i32)
      (block (result i32)
        (loop (result i32)
          (br_table 0 1 (i32.const 1) (local.get 0))
        )
      )
    )
  )
  "type mismatch"
)


(assert_invalid
  (module (func $unbound-label
    (block (br_table 2 1 (i32.const 1)))
  ))
  "unknown label"
)
(assert_invalid
  (module (func $unbound-nested-label
    (block (block (br_table 0 5 (i32.const 1))))
  ))
  "unknown label"
)
(assert_invalid
  (module (func $large-label
    (block (br_table 0 0x10000001 0 (i32.const 1)))
  ))
  "unknown label"
)

(assert_invalid
  (module (func $unbound-label-default
    (block (br_table 1 2 (i32.const 1)))
  ))
  "unknown label"
)
(assert_invalid
  (module (func $unbound-nested-label-default
    (block (block (br_table 0 5 (i32.const 1))))
  ))
  "unknown label"
)
(assert_invalid
  (module (func $large-label-default
    (block (br_table 0 0 0x10000001 (i32.const 1)))
  ))
  "unknown label"
)