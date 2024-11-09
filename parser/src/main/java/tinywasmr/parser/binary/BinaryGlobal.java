package tinywasmr.parser.binary;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import tinywasmr.engine.type.GlobalType;

public record BinaryGlobal(GlobalType type, List<BinaryInstructionBuilder> expression) {
	public static BinaryGlobal parse(InputStream stream) throws IOException {
		GlobalType type = StreamReader.parseGlobalType(stream);
		List<BinaryInstructionBuilder> expr = new ArrayList<>();
		BinaryInstructionBuilder insn;
		while ((insn = CodeParser.parseInsn(stream)) != null) expr.add(insn);
		return new BinaryGlobal(type, expr);
	}
}
