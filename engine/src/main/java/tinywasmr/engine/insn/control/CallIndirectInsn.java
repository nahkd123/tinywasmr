package tinywasmr.engine.insn.control;

import tinywasmr.engine.exec.ValidationException;
import tinywasmr.engine.exec.instance.Function;
import tinywasmr.engine.exec.table.Table;
import tinywasmr.engine.exec.value.FuncRefValue;
import tinywasmr.engine.exec.value.RefValue;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.module.table.TableDecl;
import tinywasmr.engine.type.FunctionType;

public record CallIndirectInsn(FunctionType type, TableDecl table) implements Instruction {
	@Override
	public void execute(Machine vm) {
		Table table = vm.peekFunctionFrame().getInstance().table(this.table);
		Value index = vm.peekFrame().popOprand();
		RefValue ref = table.get(index.i32());
		if (!(ref instanceof FuncRefValue funcRef))
			throw new ValidationException("Reference type mismatch: %s != funcref".formatted(ref.type()));
		Function function = funcRef.function();
		if (function == null) throw new NullPointerException("funcref is null");
		CallInsn.callFunction(vm, function);
	}
}
