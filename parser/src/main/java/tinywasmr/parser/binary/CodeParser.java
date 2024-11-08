package tinywasmr.parser.binary;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import tinywasmr.engine.exec.value.NumberF32Value;
import tinywasmr.engine.exec.value.NumberF64Value;
import tinywasmr.engine.exec.value.NumberI32Value;
import tinywasmr.engine.exec.value.NumberI64Value;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.value.Vector128Value;
import tinywasmr.engine.insn.ConstInsn;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.insn.control.BlockInsn;
import tinywasmr.engine.insn.control.BranchIfInsn;
import tinywasmr.engine.insn.control.BranchInsn;
import tinywasmr.engine.insn.control.CallIndirectInsn;
import tinywasmr.engine.insn.control.CallInsn;
import tinywasmr.engine.insn.control.ControlInsn;
import tinywasmr.engine.insn.control.IfInsn;
import tinywasmr.engine.insn.control.LoopInsn;
import tinywasmr.engine.insn.memory.LoadInsn;
import tinywasmr.engine.insn.memory.LoadType;
import tinywasmr.engine.insn.memory.MemoryArg;
import tinywasmr.engine.insn.memory.MemoryInsn;
import tinywasmr.engine.insn.memory.MemoryInsnType;
import tinywasmr.engine.insn.memory.StoreInsn;
import tinywasmr.engine.insn.memory.StoreType;
import tinywasmr.engine.insn.numeric.NumericBinaryOpInsn;
import tinywasmr.engine.insn.numeric.NumericUnaryOpInsn;
import tinywasmr.engine.insn.parametric.ParametricInsn;
import tinywasmr.engine.insn.parametric.SelectExplictInsn;
import tinywasmr.engine.insn.ref.RefFuncInsn;
import tinywasmr.engine.insn.ref.RefInsn;
import tinywasmr.engine.insn.table.TableInsn;
import tinywasmr.engine.insn.table.TableInsnType;
import tinywasmr.engine.insn.variable.LocalInsn;
import tinywasmr.engine.insn.variable.LocalInsnType;
import tinywasmr.engine.type.BlockType;
import tinywasmr.engine.type.ResultType;
import tinywasmr.engine.type.value.RefType;
import tinywasmr.engine.type.value.ValueType;

/**
 * <p>
 * Code parser parses from byte stream into instruction builder. This also
 * contains the opcodes for all instructions, in case you want to build binary
 * module from ground up.
 * </p>
 */
public class CodeParser {
	// Control instructions
	public static final int UNREACHABLE = 0x00;
	public static final int NOP = 0x01;
	public static final int BLOCK = 0x02;
	public static final int LOOP = 0x03;
	public static final int IF = 0x04;
	public static final int ELSE = 0x05;
	public static final int END = 0x0B;
	public static final int BR = 0x0C;
	public static final int BR_IF = 0x0D;
	public static final int BR_TABLE = 0x0E;
	public static final int RETURN = 0x0F;
	public static final int CALL = 0x10;
	public static final int CALL_INDIRECT = 0x11;

	// Parametric instructions
	public static final int DROP = 0x1A;
	public static final int SELECT_AUTO = 0x1B;
	public static final int SELECT_EXPLICT = 0x1C;

	// Variable instructions
	public static final int LOCAL_GET = 0x20;
	public static final int LOCAL_SET = 0x21;
	public static final int LOCAL_TEE = 0x22;
	public static final int GLOBAL_GET = 0x23;
	public static final int GLOBAL_SET = 0x24;

	// Table instructions
	public static final int TABLE_GET = 0x25;
	public static final int TABLE_SET = 0x26;

	// Memory instructions
	public static final int I32_LOAD = 0x28;
	public static final int I64_LOAD = 0x29;
	public static final int F32_LOAD = 0x2A;
	public static final int F64_LOAD = 0x2B;
	public static final int I32_LOAD8_S = 0x2C;
	public static final int I32_LOAD8_U = 0x2D;
	public static final int I32_LOAD16_S = 0x2E;
	public static final int I32_LOAD16_U = 0x2F;
	public static final int I64_LOAD8_S = 0x30;
	public static final int I64_LOAD8_U = 0x31;
	public static final int I64_LOAD16_S = 0x32;
	public static final int I64_LOAD16_U = 0x33;
	public static final int I64_LOAD32_S = 0x34;
	public static final int I64_LOAD32_U = 0x35;
	public static final int I32_STORE = 0x36;
	public static final int I64_STORE = 0x37;
	public static final int F32_STORE = 0x38;
	public static final int F64_STORE = 0x39;
	public static final int I32_STORE8 = 0x3A;
	public static final int I32_STORE16 = 0x3B;
	public static final int I64_STORE8 = 0x3C;
	public static final int I64_STORE16 = 0x3D;
	public static final int I64_STORE32 = 0x3E;
	public static final int MEMORY_SIZE = 0x3F;
	public static final int MEMORY_GROW = 0x40;

	// Numeric instructions
	public static final int I32_CONST = 0x41;
	public static final int I64_CONST = 0x42;
	public static final int F32_CONST = 0x43;
	public static final int F64_CONST = 0x44;

	public static final int I32_EQZ = 0x45;
	public static final int I32_EQ = 0x46;
	public static final int I32_NE = 0x47;
	public static final int I32_LT_S = 0x48;
	public static final int I32_LT_U = 0x49;
	public static final int I32_GT_S = 0x4A;
	public static final int I32_GT_U = 0x4B;
	public static final int I32_LE_S = 0x4C;
	public static final int I32_LE_U = 0x4D;
	public static final int I32_GE_S = 0x4E;
	public static final int I32_GE_U = 0x4F;

	public static final int I64_EQZ = 0x50;
	public static final int I64_EQ = 0x51;
	public static final int I64_NE = 0x52;
	public static final int I64_LT_S = 0x53;
	public static final int I64_LT_U = 0x54;
	public static final int I64_GT_S = 0x55;
	public static final int I64_GT_U = 0x56;
	public static final int I64_LE_S = 0x57;
	public static final int I64_LE_U = 0x58;
	public static final int I64_GE_S = 0x59;
	public static final int I64_GE_U = 0x5A;

	public static final int F32_EQ = 0x5B;
	public static final int F32_NE = 0x5C;
	public static final int F32_LT = 0x5D;
	public static final int F32_GT = 0x5E;
	public static final int F32_LE = 0x5F;
	public static final int F32_GE = 0x60;

	public static final int F64_EQ = 0x61;
	public static final int F64_NE = 0x62;
	public static final int F64_LT = 0x63;
	public static final int F64_GT = 0x64;
	public static final int F64_LE = 0x65;
	public static final int F64_GE = 0x66;

	public static final int I32_CLZ = 0x67;
	public static final int I32_CTZ = 0x68;
	public static final int I32_POPCNT = 0x69;
	public static final int I32_ADD = 0x6A;
	public static final int I32_SUB = 0x6B;
	public static final int I32_MUL = 0x6C;
	public static final int I32_DIV_S = 0x6D;
	public static final int I32_DIV_U = 0x6E;
	public static final int I32_REM_S = 0x6F;
	public static final int I32_REM_U = 0x70;
	public static final int I32_AND = 0x71;
	public static final int I32_OR = 0x72;
	public static final int I32_XOR = 0x73;
	public static final int I32_SHL = 0x74;
	public static final int I32_SHR_S = 0x75;
	public static final int I32_SHR_U = 0x76;
	public static final int I32_ROTL = 0x77;
	public static final int I32_ROTR = 0x78;

	public static final int I64_CLZ = 0x79;
	public static final int I64_CTZ = 0x7A;
	public static final int I64_POPCNT = 0x7B;
	public static final int I64_ADD = 0x7C;
	public static final int I64_SUB = 0x7D;
	public static final int I64_MUL = 0x7E;
	public static final int I64_DIV_S = 0x7F;
	public static final int I64_DIV_U = 0x80;
	public static final int I64_REM_S = 0x81;
	public static final int I64_REM_U = 0x82;
	public static final int I64_AND = 0x83;
	public static final int I64_OR = 0x84;
	public static final int I64_XOR = 0x85;
	public static final int I64_SHL = 0x86;
	public static final int I64_SHR_S = 0x87;
	public static final int I64_SHR_U = 0x88;
	public static final int I64_ROTL = 0x89;
	public static final int I64_ROTR = 0x8A;

	public static final int F32_ABS = 0x8B;
	public static final int F32_NEG = 0x8C;
	public static final int F32_CEIL = 0x8D;
	public static final int F32_FLOOR = 0x8E;
	public static final int F32_TRUNC = 0x8F;
	public static final int F32_NEAREST = 0x90;
	public static final int F32_SQRT = 0x91;
	public static final int F32_ADD = 0x92;
	public static final int F32_SUB = 0x93;
	public static final int F32_MUL = 0x94;
	public static final int F32_DIV = 0x95;
	public static final int F32_MIN = 0x96;
	public static final int F32_MAX = 0x97;
	public static final int F32_COPYSIGN = 0x98;

	public static final int F64_ABS = 0x99;
	public static final int F64_NEG = 0x9A;
	public static final int F64_CEIL = 0x9B;
	public static final int F64_FLOOR = 0x9C;
	public static final int F64_TRUNC = 0x9D;
	public static final int F64_NEAREST = 0x9E;
	public static final int F64_SQRT = 0x9F;
	public static final int F64_ADD = 0xA0;
	public static final int F64_SUB = 0xA1;
	public static final int F64_MUL = 0xA2;
	public static final int F64_DIV = 0xA3;
	public static final int F64_MIN = 0xA4;
	public static final int F64_MAX = 0xA5;
	public static final int F64_COPYSIGN = 0xA6;

	// Reference instructions
	public static final int REF_NULL = 0xD0;
	public static final int REF_IS_NULL = 0xD1;
	public static final int REF_FUNC = 0xD2;

	// Memory instructions (extended)
	public static final int MEMORY_INIT = 0xFC08;
	public static final int DATA_DROP = 0xFC09;
	public static final int MEMORY_COPY = 0xFC0A;
	public static final int MEMORY_FILL = 0xFC0B;

	// Table instructions (extended)
	public static final int TABLE_INIT = 0xFC0C;
	public static final int ELEM_DROP = 0xFC0D;
	public static final int TABLE_COPY = 0xFC0E;
	public static final int TABLE_GROW = 0xFC0F;
	public static final int TABLE_SIZE = 0xFC10;
	public static final int TABLE_FILL = 0xFC11;

	// Vector instructions
	public static final int V128_CONST = 0xFD0C;

	/**
	 * <p>
	 * Parse an instruction from byte stream.
	 * </p>
	 * 
	 * @param stream A byte stream to read instruction opcode and possibly constant
	 *               values.
	 * @return An instruction builder, which takes in a view of module's layout in
	 *         order to fully expand into {@link Instruction}.
	 * @throws IOException if I/O operation error occurred or end of stream reached.
	 */
	public static BinaryInstructionBuilder parseInsn(InputStream stream) throws IOException {
		int insn = stream.read();
		if (insn == -1) return null;
		return parseInsn(insn, stream);
	}

	/**
	 * <p>
	 * Parse an instruction. Most single-byte opcodes does not need a stream. Most
	 * multi-byte opcodes require a byte stream to read next byte, unless the opcode
	 * is already expanded, like {@link #TABLE_SIZE} for example.
	 * </p>
	 * 
	 * @param insn   The code of instruction. You can obtain one from the constants
	 *               defined in {@link CodeParser}, or parse from stream by reading
	 *               a single byte.
	 * @param stream A byte stream to read in constant value(s) for specific
	 *               instruction. Most instructions does not need a stream, like
	 *               {@link #I32_ADD} for example.
	 * @return An instruction builder, which takes in a view of module's layout in
	 *         order to fully expand into {@link Instruction}.
	 * @throws IOException if I/O operation error occurred or end of stream reached.
	 */
	public static BinaryInstructionBuilder parseInsn(int insn, InputStream stream) throws IOException {
		// @formatter:off
		switch (insn) {
		// Control instructions
		case UNREACHABLE: return $ -> ControlInsn.UNREACHABLE;
		case NOP: return $ -> ControlInsn.NOP;
		case BLOCK:
		case LOOP:
		case IF: {
			Object blockType = StreamReader.parseBlockType(stream);
			List<BinaryInstructionBuilder> primary = new ArrayList<>();
			List<BinaryInstructionBuilder> secondary = new ArrayList<>();
			List<BinaryInstructionBuilder> current = primary;
			int childInsn;

			while (true) {
				childInsn = stream.read();
				if (childInsn == -1) throw new EOFException();
				if (childInsn == END) break;

				if (childInsn == ELSE) {
					if (insn != IF) throw new IOException("Only if block can use else instruction");
					if (current == secondary) throw new IOException("Cannot use 2 consecutive else instructions");
					current = secondary;
				}

				BinaryInstructionBuilder builder = parseInsn(childInsn, stream);
				current.add(builder);
			}

			return view -> {
				BlockType unboxedBlockType;
				if (blockType instanceof BlockType bt) unboxedBlockType = bt;
				else unboxedBlockType = view.types().get((int) blockType).outputs();
				return switch (insn) {
				case BLOCK -> new BlockInsn(unboxedBlockType, primary.stream().map(b -> b.build(view)).toList());
				case LOOP -> new LoopInsn(unboxedBlockType, primary.stream().map(b -> b.build(view)).toList());
				case IF -> new IfInsn(unboxedBlockType, primary.stream().map(b -> b.build(view)).toList(), secondary.stream().map(b -> b.build(view)).toList());
				default -> throw new RuntimeException("Unreachable");
				};
			};
		}
		case END: return null;
		case BR:
		case BR_IF: {
			int idx = StreamReader.readUint32Var(stream);
			return switch (insn) {
			case BR -> $ -> new BranchInsn(idx);
			case BR_IF -> $ -> new BranchIfInsn(idx);
			default -> throw new RuntimeException("Unreachable");
			};
		}
		case RETURN: return $ -> ControlInsn.RETURN;
		case CALL: {
			int idx = StreamReader.readUint32Var(stream);
			return view -> new CallInsn(view.functions().get(idx));
		}
		case CALL_INDIRECT: {
			int typeIdx = StreamReader.readUint32Var(stream);
			int tableIdx = StreamReader.readUint32Var(stream);
			return view -> new CallIndirectInsn(view.types().get(typeIdx), view.tables().get(tableIdx));
		}

		// Parametric instructions
		case DROP: return $ -> ParametricInsn.DROP;
		case SELECT_AUTO: return $ -> ParametricInsn.SELECT_AUTO;
		case SELECT_EXPLICT: {
			ResultType resultType = StreamReader.parseResultType(stream);
			if (resultType.types().size() != 1) throw new IOException("Explict select requires exactly 1 result, found %s".formatted(resultType.types()));
			return $ -> new SelectExplictInsn(resultType.types().get(0));
		}

		// Variable instructions
		case LOCAL_GET:
		case LOCAL_SET:
		case LOCAL_TEE: {
			LocalInsnType type = switch (insn) {
			case LOCAL_GET -> LocalInsnType.GET;
			case LOCAL_SET -> LocalInsnType.SET;
			case LOCAL_TEE -> LocalInsnType.TEE;
			default -> throw new RuntimeException("Unreachable");
			};

			int index = StreamReader.readUint32Var(stream);
			return $ -> new LocalInsn(type, index);
		}

		// Table instructions
		case TABLE_GET:
		case TABLE_SET:
		case TABLE_GROW:
		case TABLE_SIZE:
		case TABLE_FILL: {
			int idx = StreamReader.readUint32Var(stream);
			return switch (insn) {
			case TABLE_GET -> view -> new TableInsn(TableInsnType.GET, view.tables().get(idx));
			case TABLE_SET -> view -> new TableInsn(TableInsnType.SET, view.tables().get(idx));
			case TABLE_GROW -> view -> new TableInsn(TableInsnType.GROW, view.tables().get(idx));
			case TABLE_SIZE -> view -> new TableInsn(TableInsnType.SIZE, view.tables().get(idx));
			case TABLE_FILL -> view -> new TableInsn(TableInsnType.FILL, view.tables().get(idx));
			default -> throw new RuntimeException("Unreachable");
			};
		}

		// Memory instructions
		case I32_LOAD:
		case I64_LOAD:
		case F32_LOAD:
		case F64_LOAD:
		case I32_LOAD8_S:
		case I32_LOAD8_U:
		case I32_LOAD16_S:
		case I32_LOAD16_U:
		case I64_LOAD8_S:
		case I64_LOAD8_U:
		case I64_LOAD16_S:
		case I64_LOAD16_U:
		case I64_LOAD32_S:
		case I64_LOAD32_U:
		case I32_STORE:
		case I64_STORE:
		case F32_STORE:
		case F64_STORE:
		case I32_STORE8:
		case I32_STORE16:
		case I64_STORE8:
		case I64_STORE16:
		case I64_STORE32: {
			MemoryArg memarg = StreamReader.parseMemarg(stream);
			int idx = 0; // Always 0 for the time being
			return switch (insn) {
			case I32_LOAD -> view -> new LoadInsn(view.memories().get(idx), LoadType.I32, memarg);
			case I64_LOAD -> view -> new LoadInsn(view.memories().get(idx), LoadType.I64, memarg);
			case F32_LOAD -> view -> new LoadInsn(view.memories().get(idx), LoadType.F32, memarg);
			case F64_LOAD -> view -> new LoadInsn(view.memories().get(idx), LoadType.F64, memarg);
			case I32_LOAD8_S -> view -> new LoadInsn(view.memories().get(idx), LoadType.I32_S8, memarg);
			case I32_LOAD8_U -> view -> new LoadInsn(view.memories().get(idx), LoadType.I32_U8, memarg);
			case I32_LOAD16_S -> view -> new LoadInsn(view.memories().get(idx), LoadType.I32_S16, memarg);
			case I32_LOAD16_U -> view -> new LoadInsn(view.memories().get(idx), LoadType.I32_U16, memarg);
			case I64_LOAD8_S -> view -> new LoadInsn(view.memories().get(idx), LoadType. I64_S8, memarg);
			case I64_LOAD8_U -> view -> new LoadInsn(view.memories().get(idx), LoadType. I64_U8, memarg);
			case I64_LOAD16_S -> view -> new LoadInsn(view.memories().get(idx), LoadType.I64_S16, memarg);
			case I64_LOAD16_U -> view -> new LoadInsn(view.memories().get(idx), LoadType.I64_U16, memarg);
			case I64_LOAD32_S -> view -> new LoadInsn(view.memories().get(idx), LoadType.I64_S32, memarg);
			case I64_LOAD32_U -> view -> new LoadInsn(view.memories().get(idx), LoadType.I64_U32, memarg);
			case I32_STORE -> view -> new StoreInsn(view.memories().get(idx), StoreType.I32, memarg);
			case I64_STORE -> view -> new StoreInsn(view.memories().get(idx), StoreType.I64, memarg);
			case F32_STORE -> view -> new StoreInsn(view.memories().get(idx), StoreType.F32, memarg);
			case F64_STORE -> view -> new StoreInsn(view.memories().get(idx), StoreType.F64, memarg);
			case I32_STORE8 -> view -> new StoreInsn(view.memories().get(idx), StoreType.I32_I8, memarg);
			case I32_STORE16 -> view -> new StoreInsn(view.memories().get(idx), StoreType.I32_I16, memarg);
			case I64_STORE8 -> view -> new StoreInsn(view.memories().get(idx), StoreType.I64_I8, memarg);
			case I64_STORE16 -> view -> new StoreInsn(view.memories().get(idx), StoreType.I64_I16, memarg);
			case I64_STORE32 -> view -> new StoreInsn(view.memories().get(idx), StoreType.I64_I32, memarg);
			default -> throw new RuntimeException("Unreachable");
			};
		}
		case MEMORY_SIZE: return view -> new MemoryInsn(MemoryInsnType.SIZE, view.memories().get(0));
		case MEMORY_GROW: return view -> new MemoryInsn(MemoryInsnType.GROW, view.memories().get(0));

		// Numeric instructions
		case I32_CONST:
		case I64_CONST:
		case F32_CONST:
		case F64_CONST: {
			Value val = switch (insn) {
			case I32_CONST -> new NumberI32Value(StreamReader.readSint32Var(stream));
			case I64_CONST -> new NumberI64Value(StreamReader.readSint64Var(stream));
			case F32_CONST -> new NumberF32Value(StreamReader.readFloat32(stream));
			case F64_CONST -> new NumberF64Value(StreamReader.readFloat64(stream));
			default -> throw new RuntimeException("Unreachable");
			};
			return $ -> new ConstInsn(val);
		}

		case I32_EQZ: return $ -> NumericUnaryOpInsn.I32_EQZ;
		case I32_EQ: return $ -> NumericBinaryOpInsn.I32_EQ;
		case I32_NE: return $ -> NumericBinaryOpInsn.I32_NE;
		case I32_LT_S: return $ -> NumericBinaryOpInsn.I32_LT_S;
		case I32_LT_U: return $ -> NumericBinaryOpInsn.I32_LT_U;
		case I32_GT_S: return $ -> NumericBinaryOpInsn.I32_GT_S;
		case I32_GT_U: return $ -> NumericBinaryOpInsn.I32_GT_U;
		case I32_LE_S: return $ -> NumericBinaryOpInsn.I32_LE_S;
		case I32_LE_U: return $ -> NumericBinaryOpInsn.I32_LE_U;
		case I32_GE_S: return $ -> NumericBinaryOpInsn.I32_GE_S;
		case I32_GE_U: return $ -> NumericBinaryOpInsn.I32_GE_U;
		case I32_CTZ: return $ -> NumericUnaryOpInsn.I32_CTZ;
		case I32_CLZ: return $ -> NumericUnaryOpInsn.I32_CLZ;
		case I32_POPCNT: return $ -> NumericUnaryOpInsn.I32_POPCNT;
		case I32_ADD: return $ -> NumericBinaryOpInsn.I32_ADD;
		case I32_SUB: return $ -> NumericBinaryOpInsn.I32_SUB;
		case I32_MUL: return $ -> NumericBinaryOpInsn.I32_MUL;
		case I32_DIV_S: return $ -> NumericBinaryOpInsn.I32_DIV_S;
		case I32_DIV_U: return $ -> NumericBinaryOpInsn.I32_DIV_U;
		case I32_REM_S: return $ -> NumericBinaryOpInsn.I32_REM_S;
		case I32_REM_U: return $ -> NumericBinaryOpInsn.I32_REM_U;
		case I32_AND: return $ -> NumericBinaryOpInsn.I32_AND;
		case I32_OR: return $ -> NumericBinaryOpInsn.I32_OR;
		case I32_XOR: return $ -> NumericBinaryOpInsn.I32_XOR;
		case I32_SHL: return $ -> NumericBinaryOpInsn.I32_SHL;
		case I32_SHR_S: return $ -> NumericBinaryOpInsn.I32_SHR_S;
		case I32_SHR_U: return $ -> NumericBinaryOpInsn.I32_SHR_U;
		case I32_ROTL: return $ -> NumericBinaryOpInsn.I32_ROTL;
		case I32_ROTR: return $ -> NumericBinaryOpInsn.I32_ROTR;

		case I64_EQZ: return $ -> NumericUnaryOpInsn.I64_EQZ;
		case I64_EQ: return $ -> NumericBinaryOpInsn.I64_EQ;
		case I64_NE: return $ -> NumericBinaryOpInsn.I64_NE;
		case I64_LT_S: return $ -> NumericBinaryOpInsn.I64_LT_S;
		case I64_LT_U: return $ -> NumericBinaryOpInsn.I64_LT_U;
		case I64_GT_S: return $ -> NumericBinaryOpInsn.I64_GT_S;
		case I64_GT_U: return $ -> NumericBinaryOpInsn.I64_GT_U;
		case I64_LE_S: return $ -> NumericBinaryOpInsn.I64_LE_S;
		case I64_LE_U: return $ -> NumericBinaryOpInsn.I64_LE_U;
		case I64_GE_S: return $ -> NumericBinaryOpInsn.I64_GE_S;
		case I64_GE_U: return $ -> NumericBinaryOpInsn.I64_GE_U;
		case I64_CTZ: return $ -> NumericUnaryOpInsn.I64_CTZ;
		case I64_CLZ: return $ -> NumericUnaryOpInsn.I64_CLZ;
		case I64_POPCNT: return $ -> NumericUnaryOpInsn.I64_POPCNT;
		case I64_ADD: return $ -> NumericBinaryOpInsn.I64_ADD;
		case I64_SUB: return $ -> NumericBinaryOpInsn.I64_SUB;
		case I64_MUL: return $ -> NumericBinaryOpInsn.I64_MUL;
		case I64_DIV_S: return $ -> NumericBinaryOpInsn.I64_DIV_S;
		case I64_DIV_U: return $ -> NumericBinaryOpInsn.I64_DIV_U;
		case I64_REM_S: return $ -> NumericBinaryOpInsn.I64_REM_S;
		case I64_REM_U: return $ -> NumericBinaryOpInsn.I64_REM_U;
		case I64_AND: return $ -> NumericBinaryOpInsn.I64_AND;
		case I64_OR: return $ -> NumericBinaryOpInsn.I64_OR;
		case I64_XOR: return $ -> NumericBinaryOpInsn.I64_XOR;
		case I64_SHL: return $ -> NumericBinaryOpInsn.I64_SHL;
		case I64_SHR_S: return $ -> NumericBinaryOpInsn.I64_SHR_S;
		case I64_SHR_U: return $ -> NumericBinaryOpInsn.I64_SHR_U;
		case I64_ROTL: return $ -> NumericBinaryOpInsn.I64_ROTL;
		case I64_ROTR: return $ -> NumericBinaryOpInsn.I64_ROTR;

		case F32_EQ: return $ -> NumericBinaryOpInsn.F32_EQ;
		case F32_NE: return $ -> NumericBinaryOpInsn.F32_NE;
		case F32_LT: return $ -> NumericBinaryOpInsn.F32_LT;
		case F32_GT: return $ -> NumericBinaryOpInsn.F32_GT;
		case F32_LE: return $ -> NumericBinaryOpInsn.F32_LE;
		case F32_GE: return $ -> NumericBinaryOpInsn.F32_GE;
		case F32_ABS: return $ -> NumericUnaryOpInsn.F32_ABS;
		case F32_NEG: return $ -> NumericUnaryOpInsn.F32_NEG;
		case F32_CEIL: return $ -> NumericUnaryOpInsn.F32_CEIL;
		case F32_FLOOR: return $ -> NumericUnaryOpInsn.F32_FLOOR;
		case F32_TRUNC: return $ -> NumericUnaryOpInsn.F32_TRUNC;
		case F32_NEAREST: return $ -> NumericUnaryOpInsn.F32_NEAREST;
		case F32_SQRT: return $ -> NumericUnaryOpInsn.F32_SQRT;
		case F32_ADD: return $ -> NumericBinaryOpInsn.F32_ADD;
		case F32_SUB: return $ -> NumericBinaryOpInsn.F32_SUB;
		case F32_MUL: return $ -> NumericBinaryOpInsn.F32_MUL;
		case F32_DIV: return $ -> NumericBinaryOpInsn.F32_DIV;
		case F32_MIN: return $ -> NumericBinaryOpInsn.F32_MIN;
		case F32_MAX: return $ -> NumericBinaryOpInsn.F32_MAX;
		case F32_COPYSIGN: return $ -> NumericBinaryOpInsn.F32_COPYSIGN;

		case F64_EQ: return $ -> NumericBinaryOpInsn.F64_EQ;
		case F64_NE: return $ -> NumericBinaryOpInsn.F64_NE;
		case F64_LT: return $ -> NumericBinaryOpInsn.F64_LT;
		case F64_GT: return $ -> NumericBinaryOpInsn.F64_GT;
		case F64_LE: return $ -> NumericBinaryOpInsn.F64_LE;
		case F64_GE: return $ -> NumericBinaryOpInsn.F64_GE;
		case F64_ABS: return $ -> NumericUnaryOpInsn. F64_ABS;
		case F64_NEG: return $ -> NumericUnaryOpInsn. F64_NEG;
		case F64_CEIL: return $ -> NumericUnaryOpInsn. F64_CEIL;
		case F64_FLOOR: return $ -> NumericUnaryOpInsn. F64_FLOOR;
		case F64_TRUNC: return $ -> NumericUnaryOpInsn. F64_TRUNC;
		case F64_NEAREST: return $ -> NumericUnaryOpInsn. F64_NEAREST;
		case F64_SQRT: return $ -> NumericUnaryOpInsn. F64_SQRT;
		case F64_ADD: return $ -> NumericBinaryOpInsn.F64_ADD;
		case F64_SUB: return $ -> NumericBinaryOpInsn.F64_SUB;
		case F64_MUL: return $ -> NumericBinaryOpInsn.F64_MUL;
		case F64_DIV: return $ -> NumericBinaryOpInsn.F64_DIV;
		case F64_MIN: return $ -> NumericBinaryOpInsn.F64_MIN;
		case F64_MAX: return $ -> NumericBinaryOpInsn.F64_MAX;
		case F64_COPYSIGN: return $ -> NumericBinaryOpInsn.F64_COPYSIGN;

		// Reference instructions
		case REF_NULL: {
			ValueType type = StreamReader.parseValueType(stream);
			if (!(type instanceof RefType refType)) throw new IOException("Expected funcref or externref, but found %s".formatted(type));
			return switch (refType) {
			case EXTERN -> $ -> RefInsn.NULL_EXTERN;
			case FUNC -> $ -> RefInsn.NULL_FUNC;
			default -> throw new RuntimeException("Unreachable");
			};
		}
		case REF_IS_NULL: return $ -> RefInsn.IS_NULL;
		case REF_FUNC: {
			int idx = StreamReader.readUint32Var(stream);
			return view -> new RefFuncInsn(view.functions().get(idx));
		}

		// Vector instructions
		case V128_CONST: {
			Vector128Value val = StreamReader.readV128(stream);
			return $ -> new ConstInsn(val);
		}

		// Multi-byte instructions
		case 0xFC:
		case 0xFD: {
			int second = stream.read();
			if (second == -1) throw new EOFException("EOF while reading 2nd byte of multi-byte instruction (0x%2x...)".formatted(insn));
			return parseInsn(insn << 8 | second, stream);
		}

		default: throw new RuntimeException("Instruction not implemented: 0x%02x".formatted(insn));
		}
		// @formatter:on
	}
}
