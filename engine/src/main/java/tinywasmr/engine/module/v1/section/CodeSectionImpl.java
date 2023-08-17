package tinywasmr.engine.module.v1.section;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tinywasmr.engine.io.LEDataInput;
import tinywasmr.engine.io.LEDataInputWithCounter;
import tinywasmr.engine.module.function.FunctionCode;
import tinywasmr.engine.module.section.CodeSection;
import tinywasmr.engine.module.type.Type;
import tinywasmr.engine.module.v1.ModuleParserImpl;
import tinywasmr.engine.module.v1.function.FunctionCodeImpl;

public class CodeSectionImpl implements CodeSection {
	private List<FunctionCode> functions;

	public CodeSectionImpl(List<FunctionCode> functions) {
		this.functions = functions;
	}

	public CodeSectionImpl(LEDataInput in, ModuleParserImpl parser) throws IOException {
		long count = in.readLEB128Unsigned();
		functions = new ArrayList<>();

		for (int i = 0; i < count; i++) {
			long functionSize = in.readLEB128Unsigned();

			var in2 = new LEDataInputWithCounter(in);
			long localBlocks = in2.readLEB128Unsigned();
			List<Type> locals = new ArrayList<>();

			for (int j = 0; j < localBlocks; j++) {
				long n = in2.readLEB128Unsigned();
				Type localType = parser.parseType(in2);
				for (int k = 0; k < n; k++) locals.add(localType);
			}

			functionSize -= in2.getTotalBytesRead();
			functions.add(new FunctionCodeImpl(locals, in2, functionSize));
		}
	}

	@Override
	public List<FunctionCode> getFunctions() { return Collections.unmodifiableList(functions); }
}
