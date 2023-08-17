package tinywasmr.engine.instruction.special;

import java.io.IOException;
import java.util.List;

import tinywasmr.engine.execution.context.ExecutionContext;
import tinywasmr.engine.instruction.Instruction;
import tinywasmr.engine.instruction.Instructions;
import tinywasmr.engine.io.LEDataInput;

public class BlockInstruction implements Instruction {
	private List<Instruction> content;
	private long label;

	public BlockInstruction(long label, List<Instruction> content) {
		this.label = label;
		this.content = content;
	}

	public BlockInstruction(LEDataInput in) throws IOException {
		label = in.readLEB128Unsigned();
		content = Instructions.parse(in);
	}

	public long getLabel() { return label; }

	@Override
	public void execute(ExecutionContext ctx) {
		// TODO
	}

	public List<Instruction> getContent() { return content; }

	@Override
	public String toString() {
		return "block " + label + " { /* " + content.size() + " insns */ }"; // TODO
	}
}
