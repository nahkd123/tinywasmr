package tinywasmr.engine.insn.ref;

import tinywasmr.engine.exec.instance.Function;
import tinywasmr.engine.exec.value.FuncRefValue;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.module.func.FunctionDecl;

public record RefFuncInsn(FunctionDecl function) implements Instruction {
	@Override
	public void execute(Machine vm) {
		Function function = vm.peekFunctionFrame().getInstance().function(this.function);
		vm.peekFrame().pushOperand(new FuncRefValue(function));
	}
}
