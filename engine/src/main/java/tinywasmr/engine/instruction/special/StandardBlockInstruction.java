package tinywasmr.engine.instruction.special;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import tinywasmr.engine.execution.context.ExecutionContext;
import tinywasmr.engine.instruction.Instruction;
import tinywasmr.engine.module.type.TypeEnum;

public class StandardBlockInstruction implements BlockInstruction {
	private TypeEnum returnType;
	private List<Instruction> instructions = new ArrayList<>();

	public StandardBlockInstruction(TypeEnum returnType) {
		this.returnType = returnType;
	}

	@Override
	public TypeEnum getReturnType() { return returnType; }

	@Override
	public Optional<List<Instruction>> getBodyBasedOnContext(ExecutionContext context) {
		return Optional.of(getPrimaryBody());
	}

	@Override
	public List<Instruction> getPrimaryBody() { return Collections.unmodifiableList(instructions); }

	public List<Instruction> getModifiablePrimary() { return instructions; }

	@Override
	public Optional<List<Instruction>> getSecondaryBody() { return Optional.empty(); }

	@Override
	public String getBlockName() { return "block"; }
}
