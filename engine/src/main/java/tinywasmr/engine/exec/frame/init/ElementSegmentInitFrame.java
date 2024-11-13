package tinywasmr.engine.exec.frame.init;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.frame.AbstractFrame;
import tinywasmr.engine.exec.table.Table;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.module.table.ActiveElementMode;
import tinywasmr.engine.module.table.ElementSegment;
import tinywasmr.engine.type.BlockType;
import tinywasmr.engine.type.ResultType;

public class ElementSegmentInitFrame extends AbstractFrame {
	private ElementSegment segment;
	private ActiveElementMode dataMode;

	public ElementSegmentInitFrame(ElementSegment segment, List<Value> operands, int step) {
		super(operands, step);
		if (!(segment.mode() instanceof ActiveElementMode dataMode))
			throw new IllegalArgumentException("Not an active segment");
		this.segment = segment;
		this.dataMode = dataMode;
	}

	public ElementSegmentInitFrame(ElementSegment segment) {
		this(segment, Collections.emptyList(), 0);
	}

	public ElementSegment getSegment() { return segment; }

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
			int offset = popOprand().i32();
			Table table = vm.peekInstancedFrame().getInstance().table(dataMode.table());
			vm.pushFrame(new InitTableFrame(segment, 0, table, offset, segment.inits().size()));
		} else {
			dataMode.offsetExpr().get(getStep()).execute(vm);
		}
	}
}
