package tinywasmr.engine.insn.table;

import tinywasmr.engine.exec.frame.TableInitFrame;
import tinywasmr.engine.exec.table.Table;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.module.table.ElementSegment;
import tinywasmr.engine.module.table.TableDecl;

public record TableInitInsn(TableDecl table, ElementSegment segment) implements Instruction {
	@Override
	public void execute(Machine vm) {
		int count = vm.peekFrame().popOprand().i32();
		int segmentOffset = vm.peekFrame().popOprand().i32();
		int tableOffset = vm.peekFrame().popOprand().i32();
		Table table = vm.peekFunctionFrame().getInstance().table(this.table);
		vm.pushFrame(new TableInitFrame(segment, table, segmentOffset, tableOffset, count));
	}
}
