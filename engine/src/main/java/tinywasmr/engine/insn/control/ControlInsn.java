package tinywasmr.engine.insn.control;

import java.util.List;

import tinywasmr.engine.exec.frame.FunctionFrame;
import tinywasmr.engine.exec.trap.ExternalTrap;
import tinywasmr.engine.exec.trap.ModuleTrap;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;

public enum ControlInsn implements Instruction {
	NOP,
	UNREACHABLE,
	RETURN;

	@Override
	public void execute(Machine vm) {
		switch (this) {
		case NOP:
			return;
		case UNREACHABLE:
			vm.setTrap(new ModuleTrap());
			return;
		case RETURN: {
			List<Value> results = vm.popFrame().getOperandStack();
			while (!(vm.peekFrame() instanceof FunctionFrame)) vm.popFrame();
			vm.popFrame();
			for (Value val : results) vm.peekFrame().pushOperand(val);
			return;
		}
		default:
			vm.setTrap(new ExternalTrap(new RuntimeException("Unable to execute %s".formatted(this))));
			return;
		}
	}
}
