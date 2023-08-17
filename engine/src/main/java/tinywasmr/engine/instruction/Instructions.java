package tinywasmr.engine.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import tinywasmr.engine.execution.exception.TrapException;
import tinywasmr.engine.instruction.impl.ConstInstructions;
import tinywasmr.engine.instruction.impl.I32Instructions;
import tinywasmr.engine.instruction.impl.LocalInstructions;
import tinywasmr.engine.instruction.impl.LogicInstructions;
import tinywasmr.engine.io.LEDataInput;
import tinywasmr.engine.util.HexString;

public final class Instructions {
	private static final FixedOpcodeInstructionFactory[] FIXED_OPCODE_FACTORIES = new FixedOpcodeInstructionFactory[256];
	private static final List<InstructionFactory> DYNAMIC_FACTORIES = new ArrayList<>();
	private static int fixedOpcodeFactories = 0;

	public static <T extends InstructionFactory> T addFactory(T factory) {
		if (factory instanceof FixedOpcodeInstructionFactory foif) {
			FIXED_OPCODE_FACTORIES[foif.getOpcode()] = foif;
			fixedOpcodeFactories++;
		} else {
			DYNAMIC_FACTORIES.add(factory);
		}

		return factory;
	}

	public static int getTotalFactories() { return fixedOpcodeFactories + DYNAMIC_FACTORIES.size(); }

	// https://webassembly.github.io/spec/core/appendix/index-instructions.html
	static {
		LogicInstructions.bootstrap(Instructions::addFactory);
		LocalInstructions.bootstrap(Instructions::addFactory);
		ConstInstructions.bootstrap(Instructions::addFactory);
		I32Instructions.bootstrap(Instructions::addFactory);
	}

	public static void parseAndConsume(LEDataInput in, Consumer<Instruction> consumer) throws IOException {
		int instr;
		Instruction lastInstr = null;

		do {
			lastInstr = null;

			instr = in.readByte();
			if (instr == -1) return;
			var factory = FIXED_OPCODE_FACTORIES[instr];
			if (factory != null) lastInstr = factory.parse(instr, in);
			else {
				for (var dynamicFactory : DYNAMIC_FACTORIES) {
					lastInstr = dynamicFactory.parse(instr, in);
					if (lastInstr != null) break;
				}
			}

			if (lastInstr == null) {
				// View 7 more bytes for context
				var bonuses = in.readBytes(7);
				var hex = Integer.toString(instr, 16);
				if (hex.length() == 1) hex = "0" + hex;
				hex += HexString.ofBytes(bonuses);

				throw new TrapException("Unimplemented opcode: " + instr + " (0x" + Integer.toString(instr, 16) + "): "
					+ hex);
			}

			consumer.accept(lastInstr);
		} while (lastInstr != LogicInstructions.END);
	}

	public static List<Instruction> parse(LEDataInput in) throws IOException {
		var list = new ArrayList<Instruction>();
		parseAndConsume(in, list::add);
		return list;
	}
}
