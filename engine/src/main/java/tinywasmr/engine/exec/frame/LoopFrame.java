package tinywasmr.engine.exec.frame;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.control.LoopInsn;
import tinywasmr.engine.type.BlockType;

public class LoopFrame extends AbstractFrame {
	private LoopInsn block;
	private boolean lastBranched = false;

	public LoopFrame(LoopInsn block, List<Value> operands, int step) {
		super(operands, step);
		this.block = block;
	}

	public LoopFrame(LoopInsn block) {
		this(block, Collections.emptyList(), 0);
	}

	public LoopInsn getBlock() { return block; }

	@Override
	public BlockType getBranchResultTypes() { return block.blockType(); }

	@Override
	public boolean isFrameFinished() { return getStep() >= block.instructions().size(); }

	@Override
	public void branchThis() {
		if (lastBranched) throw new IllegalStateException("Branch state not resetted, use nextStep() to reset");
		setStep(0);
		getOperandStack().clear();
		lastBranched = true;
	}

	@Override
	public void executeStep(Machine vm) {
		if (isFrameFinished()) return;
		block.instructions().get(getStep()).execute(vm);
	}

	@Override
	public void nextStep() {
		if (!lastBranched) incStep();
		else lastBranched = false;
	}
}
