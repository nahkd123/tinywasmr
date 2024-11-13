package tinywasmr.parser.binary;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import tinywasmr.engine.insn.ref.RefFuncInsn;
import tinywasmr.engine.module.table.ActiveElementMode;
import tinywasmr.engine.module.table.ElementMode;
import tinywasmr.engine.module.table.ElementSegment;
import tinywasmr.engine.module.table.SimpleElementMode;
import tinywasmr.engine.type.value.RefType;
import tinywasmr.engine.type.value.ValueType;

public record BinaryElementSegment(RefType type, int mode, int table, List<List<BinaryInstructionBuilder>> init, List<BinaryInstructionBuilder> offsetExpr) {

	public static final int TYPE_EXTERNREF = 0x01;
	public static final int MODE_PASSIVE = 0;
	public static final int MODE_ACTIVE = 1;
	public static final int MODE_DECLARATIVE = 2;

	public static BinaryElementSegment parse(InputStream stream) throws IOException {
		int id = StreamReader.readUint32Var(stream);
		switch (id) {
		case 0: { // offsetExpr; vec(funcidx)
			List<BinaryInstructionBuilder> offsetExpr = CodeParser.parseExpression(stream);
			int count = StreamReader.readUint32Var(stream);
			List<List<BinaryInstructionBuilder>> init = new ArrayList<>();

			for (int i = 0; i < count; i++) {
				int funcIdx = StreamReader.readUint32Var(stream);
				init.add(List.of(view -> new RefFuncInsn(view.functions().get(funcIdx))));
			}

			return new BinaryElementSegment(RefType.FUNC, MODE_ACTIVE, 0, init, offsetExpr);
		}
		case 1: { // elementkind; vec(funcidx)
			int kind = stream.read();
			RefType type = switch (kind) {
			case 0x00 -> RefType.FUNC;
			case -1 -> throw new EOFException();
			default -> throw new IOException("Element kind 0x%02x not implemented".formatted(kind));
			};

			int count = StreamReader.readUint32Var(stream);
			List<List<BinaryInstructionBuilder>> init = new ArrayList<>();

			for (int i = 0; i < count; i++) {
				int funcIdx = StreamReader.readUint32Var(stream);
				init.add(List.of(view -> new RefFuncInsn(view.functions().get(funcIdx))));
			}

			return new BinaryElementSegment(type, MODE_PASSIVE, -1, init, null);
		}
		case 5: { // reftype; vec(expr)
			ValueType valType = StreamReader.parseValueType(stream);
			if (!(valType instanceof RefType type)) throw new IOException("Not a reftype: %s".formatted(valType));
			int count = StreamReader.readUint32Var(stream);
			List<List<BinaryInstructionBuilder>> init = new ArrayList<>();
			for (int i = 0; i < count; i++) init.add(CodeParser.parseExpression(stream));
			return new BinaryElementSegment(type, MODE_PASSIVE, -1, init, null);
		}
		default:
			throw new IOException("Element segment type 0x%02x not implemented".formatted(id));
		}
	}

	public ElementSegment build(BinaryModuleLayout view) {
		ElementMode mode;

		switch (this.mode) {
		case MODE_PASSIVE:
			mode = SimpleElementMode.PASSIVE;
			break;
		case MODE_DECLARATIVE:
			mode = SimpleElementMode.DECLARATIVE;
			break;
		case MODE_ACTIVE: {
			mode = new ActiveElementMode(view.tables().get(table), offsetExpr.stream()
				.map(b -> b.build(view))
				.toList());
			break;
		}
		default:
			throw new RuntimeException("Mode %d is unknown".formatted(this.mode));
		}

		return new ElementSegment(type, init.stream()
			.map(expr -> expr.stream().map(b -> b.build(view)).toList())
			.toList(), mode);
	}
}
