package tinywasmr.engine.instruction.impl;

import java.io.IOException;
import java.util.function.Consumer;

import tinywasmr.engine.execution.exception.TrapException;
import tinywasmr.engine.instruction.FixedOpcodeInstructionFactory;
import tinywasmr.engine.instruction.Instruction;
import tinywasmr.engine.instruction.InstructionFactory;
import tinywasmr.engine.instruction.Instructions;
import tinywasmr.engine.instruction.param.NoParamsInstruction;
import tinywasmr.engine.instruction.param.U32InstructionFactory;
import tinywasmr.engine.instruction.special.BlockInstruction;
import tinywasmr.engine.instruction.special.IfBlockInstruction;
import tinywasmr.engine.instruction.special.StandardBlockInstruction;
import tinywasmr.engine.io.LEDataInput;

public class LogicInstructions {
	public static final NoParamsInstruction UNREACHABLE = new NoParamsInstruction(0x00, "unreachable", ctx -> {
		throw new TrapException();
	});
	public static final NoParamsInstruction NOP = new NoParamsInstruction(0x01, "nop", null);
	public static final FixedOpcodeInstructionFactory BLOCK = new FixedOpcodeInstructionFactory() {
		@Override
		public Instruction parse(LEDataInput in) throws IOException {
			var returnType = BlockInstruction.returnTypeFromBinaryId((int) in.readLEB128Unsigned());
			var out = new StandardBlockInstruction(returnType);
			Instructions.parseAndConsume(in, out, out.getModifiablePrimary()::add);
			return out;
		}

		@Override
		public int getOpcode() { return 0x02; }
	};
	public static final FixedOpcodeInstructionFactory IF = new FixedOpcodeInstructionFactory() {
		@Override
		public Instruction parse(LEDataInput in) throws IOException {
			var returnType = BlockInstruction.returnTypeFromBinaryId((int) in.readLEB128Unsigned());
			var out = new IfBlockInstruction(returnType);
			Instructions.parseAndConsume(in, out, out.getModifiablePrimary()::add);
			return out;
		}

		@Override
		public int getOpcode() { return 0x04; }
	};
	public static final NoParamsInstruction ELSE = new NoParamsInstruction(0x05, "else", ctx -> {});
	public static final NoParamsInstruction END = new NoParamsInstruction(0x0B, "end", ctx -> ctx.triggerReturn());
	public static final U32InstructionFactory BR = new U32InstructionFactory(0x0C, "br", (i32a, ctx) -> {
		ctx.setBranchOutDepth(i32a + 1);
	});
	public static final U32InstructionFactory BR_IF = new U32InstructionFactory(0x0D, "br_if", (i32a, ctx) -> {
		int a = ctx.getStack().popI32();
		if (a != 0) ctx.setBranchOutDepth(i32a + 1);
	});
	public static final NoParamsInstruction RETURN = new NoParamsInstruction(0x0F, "return", ctx -> ctx
		.triggerReturn());
	public static final NoParamsInstruction DROP = new NoParamsInstruction(0x1A, "drop", ctx -> ctx.getStack().pop());

	public static void bootstrap(Consumer<InstructionFactory> consumer) {
		consumer.accept(UNREACHABLE);
		consumer.accept(NOP);
		consumer.accept(BLOCK);
		consumer.accept(IF);
		consumer.accept(ELSE);
		consumer.accept(END);
		consumer.accept(BR);
		consumer.accept(BR_IF);
		consumer.accept(RETURN);
		consumer.accept(DROP);
	}
}
