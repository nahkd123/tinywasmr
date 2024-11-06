package tinywasmr.engine.insn;

import tinywasmr.engine.exec.vm.Machine;

public interface Instruction {
	/**
	 * <p>
	 * Execute this instruction. The instruction may manipulate the states of
	 * virtual machine.
	 * </p>
	 * 
	 * @param vm The virtual machine to manipulate the states.
	 */
	void execute(Machine vm);
}
