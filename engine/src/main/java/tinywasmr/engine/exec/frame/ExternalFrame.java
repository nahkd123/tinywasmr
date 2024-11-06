package tinywasmr.engine.exec.frame;

import java.util.List;

import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.insn.Instruction;
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
	public List<Instruction> getExecutingInsns() { return List.of(); }

	@Override
	public BlockType getBranchResultTypes() { return new ResultType(List.of()); }
}
