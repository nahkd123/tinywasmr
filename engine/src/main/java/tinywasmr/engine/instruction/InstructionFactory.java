package tinywasmr.engine.instruction;

import java.io.IOException;

import tinywasmr.engine.io.LEDataInput;

@FunctionalInterface
public interface InstructionFactory {
	public Instruction parse(int opcode, LEDataInput in) throws IOException;
}
