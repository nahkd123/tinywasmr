package tinywasmr.engine.instruction;

import java.io.IOException;

import tinywasmr.engine.io.LEDataInput;

public interface FixedOpcodeInstructionFactory extends InstructionFactory {
	public int getOpcode();

	public Instruction parse(LEDataInput in) throws IOException;

	@Override
	default Instruction parse(int opcode, LEDataInput in) throws IOException {
		if (opcode != getOpcode()) return null;
		return parse(in);
	}
}
