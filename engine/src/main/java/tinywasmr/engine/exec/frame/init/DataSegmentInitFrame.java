package tinywasmr.engine.exec.frame.init;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.frame.AbstractFrame;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.memory.MemoryInitInsn;
import tinywasmr.engine.module.memory.ActiveDataMode;
import tinywasmr.engine.module.memory.DataSegment;
import tinywasmr.engine.type.BlockType;
import tinywasmr.engine.type.ResultType;

public class DataSegmentInitFrame extends AbstractFrame {
	private DataSegment segment;
	private ActiveDataMode dataMode;

	public DataSegmentInitFrame(DataSegment segment, List<Value> operands, int step) {
		super(operands, step);
		if (!(segment.mode() instanceof ActiveDataMode dataMode))
			throw new IllegalArgumentException("Not an active segment");
		this.segment = segment;
		this.dataMode = dataMode;
	}

	public DataSegmentInitFrame(DataSegment segment) {
		this(segment, Collections.emptyList(), 0);
	}

	public DataSegment getSegment() { return segment; }

	@Override
	public boolean isFrameFinished() { return getStep() >= dataMode.offsetExpr().size() + 1; }

	@Override
	public void branchThis() {
		setStep(dataMode.offsetExpr().size()); // Jump to set insn instead of end of expression
	}

	@Override
	public BlockType getBranchResultTypes() { return new ResultType(Collections.emptyList()); }

	@Override
	public void executeStep(Machine vm) {
		if (isFrameFinished()) return;
		if (getStep() == dataMode.offsetExpr().size()) {
			int memOffset = popOprand().i32();
			MemoryInitInsn.execute(vm, segment, 0, dataMode.memory(), memOffset, segment.data().length);
		} else {
			dataMode.offsetExpr().get(getStep()).execute(vm);
		}
	}
}
