package tinywasmr.engine.exec.frame;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.control.BlockInsn;
import tinywasmr.engine.type.BlockType;

public class BlockFrame extends AbstractFrame {
	private BlockInsn block;

	public BlockFrame(BlockInsn block, List<Value> operands, int step) {
		super(operands, step);
		this.block = block;
	}

	public BlockFrame(BlockInsn block) {
		this(block, Collections.emptyList(), 0);
	}

	public BlockInsn getBlock() { return block; }

	@Override
	public BlockType getBranchResultTypes() { return block.blockType(); }

	@Override
	public boolean isFrameFinished() { return getStep() >= block.instructions().size(); }

	@Override
	public void branchThis() {
		setStep(block.instructions().size());
	}

	@Override
	public void executeStep(Machine vm) {
		block.instructions().get(getStep()).execute(vm);
	}
}
