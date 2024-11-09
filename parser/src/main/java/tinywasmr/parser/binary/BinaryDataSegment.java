package tinywasmr.parser.binary;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public record BinaryDataSegment(int mode, int memidx, List<BinaryInstructionBuilder> expression, byte[] data) {

	public static final int MODE_ACTIVE_0 = 0x00;
	public static final int MODE_PASSIVE = 0x01;
	public static final int MODE_ACTIVE_EXPLICT = 0x02;

	public static BinaryDataSegment parse(InputStream stream) throws IOException {
		int mode = StreamReader.readUint32Var(stream);
		int memidx = mode == MODE_ACTIVE_EXPLICT ? StreamReader.readUint32Var(stream) : 0;
		List<BinaryInstructionBuilder> expr = new ArrayList<>();

		if (mode == MODE_ACTIVE_0 || mode == MODE_ACTIVE_EXPLICT) {
			BinaryInstructionBuilder insn;
			while ((insn = CodeParser.parseInsn(stream)) != null) expr.add(insn);
		}

		int size = StreamReader.readUint32Var(stream);
		byte[] data = stream.readNBytes(size);
		return new BinaryDataSegment(mode, memidx, expr, data);
	}
}
