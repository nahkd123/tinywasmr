package tinywasmr.parser.binary;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import tinywasmr.engine.exec.value.Vector128Value;
import tinywasmr.engine.insn.memory.MemoryArg;
import tinywasmr.engine.type.BlockType;
import tinywasmr.engine.type.FunctionType;
import tinywasmr.engine.type.GlobalType;
import tinywasmr.engine.type.Limit;
import tinywasmr.engine.type.MemoryType;
import tinywasmr.engine.type.Mutability;
import tinywasmr.engine.type.ResultType;
import tinywasmr.engine.type.TableType;
import tinywasmr.engine.type.value.NumberType;
import tinywasmr.engine.type.value.RefType;
import tinywasmr.engine.type.value.ValueType;
import tinywasmr.engine.type.value.VectorType;

/**
 * <p>
 * A reader to read various type of data from {@link InputStream}.
 * </p>
 */
public class StreamReader {
	public static int readInt32LE(InputStream stream) throws IOException {
		byte[] bs = stream.readNBytes(4);
		if (bs.length != 4) throw new EOFException("Expected to read 4 bytes but EOF found");
		return (bs[0] & 0xff) | (bs[1] & 0xff) << 8 | (bs[2] & 0xff) << 16 | (bs[3] & 0xff) << 24;
	}

	public static int readUint32Var(InputStream stream) throws IOException {
		int result = 0;
		int shift = 0;

		while (true) {
			int b = stream.read();
			if (b == -1) break;

			result |= (b & 0x7f) << shift;
			if ((b & 0x80) == 0) break;
			shift += 7;
		}

		return result;
	}

	public static long readUint64Var(InputStream stream) throws IOException {
		long result = 0;
		long shift = 0;

		while (true) {
			int b = stream.read();
			if (b == -1) break;

			result |= (b & 0x7f) << shift;
			if ((b & 0x80) == 0) break;
			shift += 7L;
		}

		return result;
	}

	public static int readSint32Var(InputStream stream) throws IOException {
		int result = 0;
		int shift = 0;
		int b = 0;

		do {
			int b2 = stream.read();
			if (b2 == -1) break;

			b = b2;
			result |= (b & 0x7f) << shift;
			shift += 7;
		} while ((b & 0x80) != 0);

		if ((shift < 32) && (b & 0x40) != 0) result |= (~0 << shift);
		return result;
	}

	public static long readSint64Var(InputStream stream) throws IOException {
		long result = 0;
		long shift = 0;
		int b = 0;

		do {
			int b2 = stream.read();
			if (b2 == -1) break;

			b = b2;
			result |= (b & 0x7f) << shift;
			shift += 7L;
		} while ((b & 0x80) != 0);

		if ((shift < 64) && (b & 0x40) != 0) result |= (~0L << shift);
		return result;
	}

	public static float readFloat32(InputStream stream) throws IOException {
		int i = readInt32LE(stream);
		return Float.intBitsToFloat(i);
	}

	public static double readFloat64(InputStream stream) throws IOException {
		byte[] bs = stream.readNBytes(8);
		if (bs.length != 8) throw new EOFException("Expected to read 8 bytes but EOF found");
		long i = (bs[0] & 0xffL)
			| (bs[1] & 0xffL) << 8L
			| (bs[2] & 0xffL) << 16L
			| (bs[3] & 0xffL) << 24L
			| (bs[4] & 0xffL) << 32L
			| (bs[5] & 0xffL) << 40L
			| (bs[6] & 0xffL) << 48L
			| (bs[7] & 0xffL) << 56L;
		return Double.longBitsToDouble(i);
	}

	public static Vector128Value readV128(InputStream stream) throws IOException {
		byte[] bs = stream.readNBytes(16);
		if (bs.length != 8) throw new EOFException("Expected to read 16 bytes but EOF found");
		long lsb = (bs[0] & 0xffL)
			| (bs[1] & 0xffL) << 8L
			| (bs[2] & 0xffL) << 16L
			| (bs[3] & 0xffL) << 24L
			| (bs[4] & 0xffL) << 32L
			| (bs[5] & 0xffL) << 40L
			| (bs[6] & 0xffL) << 48L
			| (bs[7] & 0xffL) << 56L;
		long msb = (bs[8] & 0xffL)
			| (bs[9] & 0xffL) << 8L
			| (bs[10] & 0xffL) << 16L
			| (bs[11] & 0xffL) << 24L
			| (bs[12] & 0xffL) << 32L
			| (bs[13] & 0xffL) << 40L
			| (bs[14] & 0xffL) << 48L
			| (bs[15] & 0xffL) << 56L;
		return new Vector128Value(msb, lsb);
	}

	public static char readUtf8(InputStream stream) throws IOException {
		int ch = stream.read();
		if (ch == -1) throw new EOFException();
		if (ch < 0x80) return (char) ch;

		int next;

		if ((ch & 0b111_00000) == 0b110_00000) {
			next = 1;
			ch &= 0b000_11111;
		} else if ((ch & 0b1111_0000) == 0b1110_0000) {
			next = 2;
			ch &= 0b0000_1111;
		} else if ((ch & 0b11111_000) == 0b11110_000) {
			next = 3;
			ch &= 0b00000_111;
		} else {
			return (char) ch;
		}

		for (int i = 0; i < next; i++) {
			int b = stream.read();

			if (b == -1) {
				ch <<= 6 * (next - i - 1);
				return (char) ch;
			}

			ch <<= 6;
			ch |= b & 0b00_111111;
		}

		return (char) ch;
	}

	public static String readName(InputStream stream) throws IOException {
		int len = readUint32Var(stream);
		byte[] bs = stream.readNBytes(len);
		if (bs.length != len) throw new IOException("Expected %d bytes but only %d found".formatted(len, bs.length));
		return new String(bs, StandardCharsets.UTF_8);
	}

	public static ValueType parseValueType(int id) throws IOException {
		return switch (id) {
		case 0x7f -> NumberType.I32;
		case 0x7e -> NumberType.I64;
		case 0x7d -> NumberType.F32;
		case 0x7c -> NumberType.F64;
		case 0x7b -> VectorType.V128;
		case 0x70 -> RefType.FUNC;
		case 0x6f -> RefType.EXTERN;
		default -> null;
		};
	}

	public static ValueType parseValueType(InputStream stream) throws IOException {
		int id = stream.read();
		if (id == -1) throw new EOFException();
		ValueType valueType = parseValueType(id);
		if (valueType == null) throw new IOException("Value type not implemented: 0x%02x".formatted(id));
		return valueType;
	}

	public static ResultType parseResultType(InputStream stream) throws IOException {
		int len = StreamReader.readUint32Var(stream);
		List<ValueType> types = new ArrayList<>();
		for (int i = 0; i < len; i++) types.add(parseValueType(stream));
		return new ResultType(types);
	}

	public static FunctionType parseFunctionType(int id, InputStream stream) throws IOException {
		if (id != 0x60) return null;
		ResultType params = parseResultType(stream);
		ResultType results = parseResultType(stream);
		return new FunctionType(params, results);
	}

	public static FunctionType parseFunctionType(InputStream stream) throws IOException {
		int id = stream.read();
		if (id == -1) throw new EOFException();
		FunctionType functionType = parseFunctionType(id, stream);
		if (functionType == null)
			throw new IOException("Function type ID must be 0x60, but 0x%02x found".formatted(id));
		return functionType;
	}

	public static Limit parseLimit(InputStream stream) throws IOException {
		int type = stream.read();
		return switch (type) {
		case 0x00 -> new Limit(StreamReader.readUint32Var(stream));
		case 0x01 -> new Limit(StreamReader.readUint32Var(stream), StreamReader.readUint32Var(stream));
		case -1 -> throw new EOFException();
		default -> throw new IOException("Limit type not implemented: 0x%02x".formatted(type));
		};
	}

	public static MemoryType parseMemoryType(InputStream stream) throws IOException {
		return new MemoryType(parseLimit(stream));
	}

	public static TableType parseTableType(InputStream stream) throws IOException {
		ValueType valueType = parseValueType(stream); // TODO replace with parseRefType()
		if (!(valueType instanceof RefType refType))
			throw new IOException("Expected RefType, but %s found".formatted(valueType));
		Limit limit = parseLimit(stream);
		return new TableType(limit, refType);
	}

	public static GlobalType parseGlobalType(InputStream stream) throws IOException {
		ValueType valueType = parseValueType(stream);
		int mutablityId = stream.read();
		Mutability mutablity = switch (mutablityId) {
		case 0x00 -> Mutability.CONST;
		case 0x01 -> Mutability.VAR;
		case -1 -> throw new EOFException();
		default -> throw new IOException("Mutablity type not implemented: 0x%02x".formatted(mutablityId));
		};
		return new GlobalType(mutablity, valueType);
	}

	/**
	 * <p>
	 * Parse the block type from byte stream. The result can be any of the
	 * following:
	 * <ul>
	 * <li>An {@code int} pointing to type that was declared in the module (we do
	 * not know which type exactly, but we know the index of the type when building
	 * the module).</li>
	 * <li>{@link ResultType} that is empty, if the block type is empty.</li>
	 * <li>A {@link ValueType} if the block only return a single value.</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Note that both {@link ResultType} and {@link ValueType} implements
	 * {@link BlockType}.
	 * </p>
	 * 
	 * @param stream The byte stream to read from.
	 * @return The parsed {@link BlockType} or {@code int}.
	 * @throws IOException if I/O operation failed or end of file reached.
	 */
	public static Object parseBlockType(InputStream stream) throws IOException {
		int id = StreamReader.readSint32Var(stream);
		if (id >= 0) return id;
		id = 0x80 + id;
		if (id == 0x40) return new ResultType(List.of());
		ValueType valueType = parseValueType(id);
		if (valueType != null) return valueType;
		throw new IOException("Block result opcode not implemented: 0x%02x".formatted(id));
	}

	public static MemoryArg parseMemarg(InputStream stream) throws IOException {
		int align = readUint32Var(stream);
		int offset = readUint32Var(stream);
		return new MemoryArg(offset, align);
	}
}
