package tinywasmr.engine.insn.table;

import tinywasmr.engine.exec.table.Table;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.module.table.TableDecl;

public record TableInsn(TableInsnType type, TableDecl table) implements Instruction {
	@Override
	public void execute(Machine vm) {
		Table table = vm.peekInstancedFrame().getInstance().table(this.table);
		type.execute(vm, table);
	}
}
