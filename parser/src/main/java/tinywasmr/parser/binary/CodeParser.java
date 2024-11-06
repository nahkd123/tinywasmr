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
import tinywasmr.engine.insn.numeric.BinaryOpInsn;
import tinywasmr.engine.insn.variable.LocalInsn;
import tinywasmr.engine.insn.variable.LocalInsnType;
import tinywasmr.engine.type.BlockType;

class CodeParser {
	public static InstructionBuilder parseInsn(BinaryModuleParser moduleParser, InputStream stream) throws IOException {
		int insn = stream.read();
		if (insn == -1) return null;
		return parseInsn(moduleParser, insn, stream);
	}

	// Control instructions
	public static final int UNREACHABLE = 0x00; // [] => trap
	public static final int NOP = 0x01; // [] => []
	public static final int BLOCK = 0x02; // [] => ???
	public static final int LOOP = 0x03; // [] => ???
	public static final int IF = 0x04; // [a] => a != 0 ? do : else
	public static final int ELSE = 0x05; // marker opcode
	public static final int END = 0x0B; // marker opcode, end of function
	public static final int BR = 0x0C; // [] => ???
	public static final int BR_IF = 0x0D; // [a] => ???
	public static final int BR_TABLE = 0x0E;
	public static final int RETURN = 0x0F;
	public static final int CALL = 0x10; // [...a] => function(...a)
	public static final int CALL_INDIRECT = 0x11;

	// Reference instructions
	public static final int REF_NULL = 0xD0; // [] => [null of refType]
	public static final int REF_IS_NULL = 0xD1; // [a] => is a null ? 1.i32 : 0.i32
	public static final int REF_FUNC = 0xD2; // [] => [reference to function]

	// Parametric instructions
	public static final int DROP = 0x1A; // [a, b] => [a]
	public static final int SELECT = 0x1B; // [a, b, c] => [c == 0 ? a : b]
	public static final int SELECT_MULTI = 0x1C; // [...a, ...b, c] => [c == 0 ? ...a : ...b]

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

	public static final int I32_CMPBASE = 0x45;
	public static final int I64_CMPBASE = 0x50;
	public static final int ICMP_EQZ = 0;
	public static final int ICMP_EQ = 1;
	public static final int ICMP_NE = 2;
	public static final int ICMP_LT_S = 3;
	public static final int ICMP_LT_U = 4;
	public static final int ICMP_GT_S = 5;
	public static final int ICMP_GT_U = 6;
	public static final int ICMP_LE_S = 7;
	public static final int ICMP_LE_U = 8;
	public static final int ICMP_GE_S = 9;
	public static final int ICMP_GE_U = 10;

	public static final int I32_MATBASE = 0x67;
	public static final int I64_MATBASE = 0x79;
	public static final int IMAT_CLZ = 0;
	public static final int IMAT_CTZ = 1;
	public static final int IMAT_POPCNT = 2;
	public static final int IMAT_ADD = 3;
	public static final int IMAT_SUB = 4;
	public static final int IMAT_MUL = 5;
	public static final int IMAT_DIV_S = 6;
	public static final int IMAT_DIV_U = 7;
	public static final int IMAT_REM_S = 8;
	public static final int IMAT_REM_U = 9;
	public static final int IMAT_AND = 10;
	public static final int IMAT_OR = 11;
	public static final int IMAT_XOR = 12;
	public static final int IMAT_SHL = 13;
	public static final int IMAT_SHR_S = 14;
	public static final int IMAT_SHR_U = 15;
	public static final int IMAT_ROTL = 16;
	public static final int IMAT_ROTR = 17;

	public static final int F32_CMPBASE = 0x5B;
	public static final int F64_CMPBASE = 0x61;
	public static final int FCMP_EQ = 0;
	public static final int FCMP_NE = 1;
	public static final int FCMP_LT = 2;
	public static final int FCMP_GT = 3;
	public static final int FCMP_LE = 4;
	public static final int FCMP_GE = 5;

	public static final int F32_MATBASE = 0x8B;
	public static final int F64_MATBASE = 0x99;
	public static final int FMAT_ABS = 0;
	public static final int FMAT_NEG = 1;
	public static final int FMAT_CEIL = 2;
	public static final int FMAT_FLOOR = 3;
	public static final int FMAT_TRUNC = 4;
	public static final int FMAT_NEAREST = 5;
	public static final int FMAT_SQRT = 6;
	public static final int FMAT_ADD = 7;
	public static final int FMAT_SUB = 8;
	public static final int FMAT_MUL = 9;
	public static final int FMAT_DIV = 10;
	public static final int FMAT_MIN = 11;
	public static final int FMAT_MAX = 12;
	public static final int FMAT_COPYSIGN = 13;

	public static InstructionBuilder parseInsn(BinaryModuleParser moduleParser, int insn, InputStream stream) throws IOException {
		switch (insn) {
		case UNREACHABLE:
			return $ -> ControlInsn.UNREACHABLE;
		case NOP:
			return $ -> ControlInsn.NOP;
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
				case IF -> new IfInsn(unboxedBlockType, primary.stream().map(b -> b.build(view)).toList(), secondary
					.stream().map(b -> b.build(view)).toList());
				default -> throw new RuntimeException("Unreachable");
				};
			};
		}
		case END:
			return null;
		case RETURN:
			return $ -> ControlInsn.RETURN;
		case CALL: {
			int idx = StreamReader.readUint32Var(stream);
			return view -> new CallInsn(view.functions().get(idx));
		}
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
		case I32_MATBASE + IMAT_ADD:
			return $ -> BinaryOpInsn.I32_ADD;
		default:
			throw new RuntimeException("Instruction not implemented: 0x%02x".formatted(insn));
		}
	}
}
