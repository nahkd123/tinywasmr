package tinywasmr.engine.module.function;

import java.util.List;

import tinywasmr.engine.instruction.Instruction;
import tinywasmr.engine.module.type.Type;

public interface FunctionCode {
	public Function getFunction();

	public List<Type> getLocals();

	public List<Instruction> getInstructions();
}
