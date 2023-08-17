package tinywasmr.engine.execution.vm;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import tinywasmr.engine.execution.exception.TrapException;
import tinywasmr.engine.execution.stack.MachineStack;
import tinywasmr.engine.execution.value.ValueHolder;
import tinywasmr.engine.instruction.Instructions;
import tinywasmr.engine.io.SeekableLEDataInput;

public class VirtualMachineImpl implements VirtualMachine {
	// TODO memory and globals and whatever that is
	// maybe load from WasmModule?

	@Override
	public void execute(SeekableLEDataInput code, MachineStack stack, List<ValueHolder> locals) throws IOException {
		if (locals == null) locals = Collections.emptyList();

		int instr;
		int i32a, i32b; // "registers"
		long i64a, i64b;
		float f32a, f32b;
		ValueHolder frame;

		while ((instr = code.readByte()) != -1) {
			switch (instr) {

			case Instructions.NOP:
				continue;
			case Instructions.UNREACHABLE:
				throw new TrapException("Unreachable");
			case Instructions.DROP:
				stack.pop();
				break;

			case Instructions.END:
			case Instructions.RETURN:
				// TODO return
				return;

			case Instructions.LOCAL_GET:
				i64a = code.readLEB128();
				if (i64a >= locals.size()) throw new TrapException("Invalid local (" + i64a + ")");
				stack.push(locals.get((int) i64a));
				break;
			case Instructions.LOCAL_SET:
				i64a = code.readLEB128();
				if (i64a >= locals.size()) throw new TrapException("Invalid local (" + i64a + ")");
				stack.pop().copyTo(locals.get((int) i64a));
				break;
			case Instructions.LOCAL_TEE:
				i64a = code.readLEB128();
				if (i64a >= locals.size()) throw new TrapException("Invalid local (" + i64a + ")");
				frame = stack.pop();
				frame.copyTo(locals.get((int) i64a));
				stack.push(frame);
				break;

			case Instructions.I32_CONST:
				stack.pushI32((int) code.readLEB128());
				break;
			case Instructions.I64_CONST:
				stack.pushI64(code.readLEB128());
				break;
			case Instructions.F32_CONST:
				stack.pushF32(code.readF32());
				break;
			case Instructions.F64_CONST:
				stack.pushF64(code.readF64());
				break;

			case Instructions.I32_ADD:
				i32b = stack.popI32();
				i32a = stack.popI32();
				stack.pushI32(i32a + i32b);
				break;

			case Instructions.F32_ADD:
				f32b = stack.popF32();
				f32a = stack.popF32();
				stack.pushF32(f32a + f32b);
				break;

			default:
				throw new TrapException("Unimplemented opcode: " + instr + " (Hex 0x" + Integer.toString(instr, 16)
					+ ")");
			}
		}
	}
}
