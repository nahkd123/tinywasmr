package tinywasmr.parser.binary;

import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.insn.control.CallInsn;
import tinywasmr.engine.insn.table.TableInsn;

@FunctionalInterface
public interface BinaryInstructionBuilder {
	/**
	 * <p>
	 * Build the instruction from module's layout. Some instructions, such as
	 * {@link TableInsn} or {@link CallInsn} requires the module layout view in
	 * order to expand from indices to declaration.
	 * </p>
	 * 
	 * @param view The view of module's layout.
	 * @return The instruction.
	 */
	Instruction build(BinaryModuleLayout view);
}