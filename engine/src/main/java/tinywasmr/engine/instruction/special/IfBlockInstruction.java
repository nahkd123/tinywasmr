package tinywasmr.engine.instruction.special;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import tinywasmr.engine.execution.context.ExecutionContext;
import tinywasmr.engine.instruction.Instruction;
import tinywasmr.engine.module.type.TypeEnum;

public class IfBlockInstruction extends StandardBlockInstruction {
	private List<Instruction> secondary;

	public IfBlockInstruction(TypeEnum returnType) {
		super(returnType);
	}

	@Override
	public Optional<List<Instruction>> getBodyBasedOnContext(ExecutionContext context) {
		var v = context.getStack().popI32();
		if (v != 0) return Optional.of(getPrimaryBody());
		else return getSecondaryBody();
	}

	@Override
	public Optional<List<Instruction>> getSecondaryBody() {
		return Optional.ofNullable(secondary).map(Collections::unmodifiableList);
	}

	public void setSecondary(List<Instruction> secondary) { this.secondary = secondary; }

	@Override
	public String getBlockName() { return "if"; }
}
