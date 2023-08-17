package tinywasmr.engine.module.v1.function;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import tinywasmr.engine.instruction.Instruction;
import tinywasmr.engine.instruction.Instructions;
import tinywasmr.engine.io.LEDataInput;
import tinywasmr.engine.io.LEDataInputWithCounter;
import tinywasmr.engine.module.function.Function;
import tinywasmr.engine.module.function.FunctionCode;
import tinywasmr.engine.module.type.Type;

public class FunctionCodeImpl implements FunctionCode {
	private List<Type> locals;
	private List<Instruction> instructions;

	private Function linkedFunction;

	public FunctionCodeImpl(List<Type> locals, List<Instruction> instructions) {
		this.locals = locals;
		this.instructions = instructions;
	}

	public FunctionCodeImpl(List<Type> locals, LEDataInput in, long size) throws IOException {
		var in2 = new LEDataInputWithCounter(in);
		this.locals = locals;
		this.instructions = Instructions.parse(in2);

		var leftover = size - in2.getTotalBytesRead();
		in.readBytes((int) leftover); // TODO should we store this somewhere?
	}

	public void linkTo(Function function) {
		this.linkedFunction = function;
	}

	@Override
	public Function getFunction() {
		if (linkedFunction == null) throw new IllegalStateException("Not yet linked");
		return linkedFunction;
	}

	@Override
	public List<Type> getLocals() { return Collections.unmodifiableList(locals); }

	@Override
	public List<Instruction> getInstructions() { return Collections.unmodifiableList(instructions); }

}
