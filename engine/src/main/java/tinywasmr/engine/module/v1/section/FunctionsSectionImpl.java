package tinywasmr.engine.module.v1.section;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tinywasmr.engine.io.LEDataInput;
import tinywasmr.engine.module.function.Function;
import tinywasmr.engine.module.section.FunctionsSection;
import tinywasmr.engine.module.v1.function.FunctionImpl;

public class FunctionsSectionImpl implements FunctionsSection {
	private List<Function> functions;

	public FunctionsSectionImpl(List<Function> functions) {
		this.functions = functions;
	}

	public FunctionsSectionImpl(LEDataInput in) throws IOException {
		long count = in.readLEB128Unsigned();
		functions = new ArrayList<>();
		for (int i = 0; i < count; i++) functions.add(new FunctionImpl((int) in.readLEB128Unsigned()));
	}

	@Override
	public List<Function> getFunctions() { return Collections.unmodifiableList(functions); }
}
