package tinywasmr.engine.insn.control;

import java.util.List;

import tinywasmr.engine.exec.ValidationException;
import tinywasmr.engine.exec.frame.Frame;
import tinywasmr.engine.exec.frame.FunctionFrame;
import tinywasmr.engine.exec.trap.ModuleTrap;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.type.value.ValueType;

public enum ControlInsn implements Instruction {
	NOP {
		@Override
		public void execute(Machine vm) {}
	},
	UNREACHABLE {
		@Override
		public void execute(Machine vm) {
			vm.setTrap(new ModuleTrap());
		}
	},
	RETURN {
		@Override
		public void execute(Machine vm) {
			FunctionFrame func = vm.peekFunctionFrame();
			List<ValueType> resultTypes = vm.peekFunctionFrame().getBranchResultTypes().blockResults();
			Value[] results = new Value[resultTypes.size()];

			for (int i = results.length - 1; i >= 0; i--) {
				Value val = vm.peekFrame().popOprand();
				if (vm.hasRuntimeValidation() && !val.type().equals(resultTypes.get(i)))
					throw new ValidationException("Type mismatch: %s (stack) != %s (block)"
						.formatted(val.type(), resultTypes.get(i)));
				results[i] = val;
			}

			Frame poppedFrame;
			while ((poppedFrame = vm.popFrame()) != func) {
				if (poppedFrame instanceof FunctionFrame && poppedFrame != func)
					throw new RuntimeException("Popped wrong function frame, check VM implementation");
			}

			for (Value val : results) vm.peekFrame().pushOperand(val);
			return;
		}
	};

	@Override
	public abstract void execute(Machine vm);
}
