package tinywasmr.engine.exec.frame;

import java.util.List;

import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.insn.control.InstructionWithBlock;
import tinywasmr.engine.type.BlockType;

public class BlockFrame extends AbstractFrame {
	private InstructionWithBlock block;
	private boolean isPrimary;

	public BlockFrame(InstructionWithBlock block, List<Value> operands, int insn, boolean isPrimary) {
		super(operands, insn);
		this.block = block;
		this.isPrimary = isPrimary;
	}

	public BlockFrame(InstructionWithBlock block, List<Value> operands, int insn) {
		this(block, operands, insn, true);
	}

	public BlockFrame(InstructionWithBlock block, boolean isPrimary) {
		this(block, List.of(), 0, isPrimary);
	}

	public BlockFrame(InstructionWithBlock block) {
		this(block, List.of(), 0, true);
	}

	/**
	 * <p>
	 * Get the block that this frame is executing.
	 * </p>
	 */
	public InstructionWithBlock getBlock() { return block; }

	/**
	 * <p>
	 * Whether to use the primary sequence of instructions from the block.
	 * </p>
	 */
	public boolean isPrimary() { return isPrimary; }

	@Override
	public List<Instruction> getExecutingInsns() { return isPrimary ? block.primary() : block.secondary(); }

	@Override
	public BlockType getBranchResultTypes() { return block.blockType(); }
}
