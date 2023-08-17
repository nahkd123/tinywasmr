package tinywasmr.engine.instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import tinywasmr.engine.execution.exception.TrapException;
import tinywasmr.engine.instruction.impl.I32OpInstructions;
import tinywasmr.engine.instruction.impl.LocalInstructions;
import tinywasmr.engine.instruction.impl.LogicInstructions;
import tinywasmr.engine.io.LEDataInput;

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
		I32OpInstructions.bootstrap(Instructions::addFactory);
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

			if (lastInstr == null) throw new TrapException("Unimplemented opcode: " + instr + " (0x"
				+ Integer.toString(instr, 16) + ")");

			consumer.accept(lastInstr);
		} while (lastInstr != LogicInstructions.END);
	}

	public static List<Instruction> parse(LEDataInput in) throws IOException {
		var list = new ArrayList<Instruction>();
		parseAndConsume(in, list::add);
		return list;
	}
}
