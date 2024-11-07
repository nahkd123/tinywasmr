package tinywasmr.parser.binary;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import tinywasmr.engine.insn.control.BlockInsn;
import tinywasmr.engine.insn.control.CallInsn;
import tinywasmr.engine.insn.control.ControlInsn;
import tinywasmr.engine.insn.control.IfInsn;
import tinywasmr.engine.insn.control.LoopInsn;
import tinywasmr.engine.insn.numeric.NumericBinaryOpInsn;
import tinywasmr.engine.insn.numeric.NumericUnaryOpInsn;
import tinywasmr.engine.insn.variable.LocalInsn;
import tinywasmr.engine.insn.variable.LocalInsnType;
import tinywasmr.engine.type.BlockType;

public class CodeParser {
	public static InstructionBuilder parseInsn(BinaryModuleParser moduleParser, InputStream stream) throws IOException {
		int insn = stream.read();
		if (insn == -1) return null;
		return parseInsn(moduleParser, insn, stream);
	}

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
	public static final int SELECT = 0x1B;
	public static final int SELECT_MULTI = 0x1C;

	// Variable instructions
	public static final int LOCAL_GET = 0x20;
	public static final int LOCAL_SET = 0x21;
	public static final int LOCAL_TEE = 0x22;
	public static final int GLOBAL_GET = 0x23;
	public static final int GLOBAL_SET = 0x24;

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

	public static InstructionBuilder parseInsn(BinaryModuleParser moduleParser, int insn, InputStream stream) throws IOException {
		// @formatter:off
		switch (insn) {
		// Control instructions
		case UNREACHABLE: return $ -> ControlInsn.UNREACHABLE;
		case NOP: return $ -> ControlInsn.NOP;
		case BLOCK:
		case LOOP:
		case IF: {
			Object blockType = moduleParser.parseBlockType(stream);
			List<InstructionBuilder> primary = new ArrayList<>();
			List<InstructionBuilder> secondary = new ArrayList<>();
			List<InstructionBuilder> current = primary;
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

				InstructionBuilder builder = parseInsn(moduleParser, childInsn, stream);
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
		case RETURN: return $ -> ControlInsn.RETURN;
		case CALL: {
			int idx = StreamReader.readUint32Var(stream);
			return view -> new CallInsn(view.functions().get(idx));
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

		// Numeric instructions
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
		default: throw new RuntimeException("Instruction not implemented: 0x%02x".formatted(insn));
		}
		// @formatter:on
	}
}
