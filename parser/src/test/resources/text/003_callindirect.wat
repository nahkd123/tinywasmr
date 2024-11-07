(module
  (table $t 1 funcref)
  (func (export "main") (param funcref)      ;; void main(void(*param1))
    i32.const 0              ;; index in table
    local.get 0              ;; param1
    table.set $t
    i32.const 0              ;; index in table
    call_indirect $t                         ;; param1();
  )
)