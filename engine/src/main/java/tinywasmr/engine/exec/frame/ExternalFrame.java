package tinywasmr.engine.exec.frame;

import java.util.List;

import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.type.BlockType;
import tinywasmr.engine.type.ResultType;

public class ExternalFrame extends AbstractFrame {
	public ExternalFrame(List<Value> operands) {
		super(operands, 0);
	}

	public ExternalFrame() {
		this(List.of());
	}

	@Override
	public BlockType getBranchResultTypes() { return new ResultType(List.of()); }

	@Override
	public boolean isFrameFinished() { return false; }

	@Override
	public void branchThis() {
		throw new IllegalCallerException("Cannot branch external frame");
	}

	@Override
	public void executeStep(Machine vm) {
		throw new IllegalCallerException("Cannot step in external frame");
	}
}
