package tinywasmr.engine.insn.control;

import java.util.List;

import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.type.BlockType;

public interface InstructionWithBlock extends Instruction {
	/**
	 * <p>
	 * The result values to push to parent frame's operands upon exiting. This will
	 * only be used for validation.
	 * </p>
	 */
	BlockType blockType();

	/**
	 * <p>
	 * The primary sequence of instructions in this block. Primary sequence is used
	 * in all type of blocks, and it is used as true path for {@code if}
	 * instruction.
	 * </p>
	 */
	List<Instruction> primary();

	/**
	 * <p>
	 * The secondary sequence of instructions in this block. Secondary sequence is
	 * only used in {@code if} instruction for false path and is empty by default on
	 * all other block instructions.
	 * </p>
	 */
	default List<Instruction> secondary() {
		return List.of();
	}
}
