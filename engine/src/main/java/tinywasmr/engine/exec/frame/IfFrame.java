package tinywasmr.engine.exec.frame;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.control.IfInsn;
import tinywasmr.engine.type.BlockType;

public class IfFrame extends AbstractFrame {
	private IfInsn block;
	private boolean trueBranch;

	public IfFrame(IfInsn block, boolean trueBranch, List<Value> operands, int step) {
		super(operands, step);
		this.block = block;
		this.trueBranch = trueBranch;
	}

	public IfFrame(IfInsn block, boolean trueBranch) {
		this(block, trueBranch, Collections.emptyList(), 0);
	}

	public IfInsn getBlock() { return block; }

	public boolean isTrueBranch() { return trueBranch; }

	@Override
	public BlockType getBranchResultTypes() { return block.blockType(); }

	@Override
	public boolean isFrameFinished() { return getStep() >= (trueBranch ? block.truePath() : block.falsePath()).size(); }

	@Override
	public void branchThis() {
		setStep((trueBranch ? block.truePath() : block.falsePath()).size());
	}

	@Override
	public void executeStep(Machine vm) {
		if (isFrameFinished()) return;
		(trueBranch ? block.truePath() : block.falsePath()).get(getStep()).execute(vm);
	}
}
