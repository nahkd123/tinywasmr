package tinywasmr.engine.insn.control;

import java.util.List;

import tinywasmr.engine.exec.ValidationException;
import tinywasmr.engine.exec.frame.FunctionFrame;
import tinywasmr.engine.exec.instance.Function;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.module.func.ExternalFunctionDecl;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.type.value.ValueType;

public record CallInsn(FunctionDecl function) implements Instruction {
	@Override
	public void execute(Machine vm) {
		Function function = vm.peekFunctionFrame().getInstance().function(this.function);

		List<ValueType> paramTypes = function.type().inputs().types();
		Value[] params = new Value[paramTypes.size()];

		for (int i = params.length - 1; i >= 0; i--) {
			Value val = vm.peekFrame().popOprand();

			if (vm.hasRuntimeValidation() && !val.type().equals(paramTypes.get(i)))
				throw new ValidationException("Type mismatch: %s (stack) != %s (declared)"
					.formatted(val.type(), paramTypes.get(i)));

			params[i] = val;
		}

		if (function.declaration() instanceof ExternalFunctionDecl extern) {
			Value[] results = extern.onExec(function.instance(), params);
			List<ValueType> resultTypes = function.type().outputs().types();

			for (int i = 0; i < results.length; i++) {
				Value val = results[i];
				if (vm.hasRuntimeValidation() && val.type().equals(resultTypes.get(i)))
					throw new ValidationException("Type mismatch: %s (extern) != %s (declared)"
						.formatted(val.type(), resultTypes.get(i)));
				vm.peekFrame().pushOperand(val);
			}
		} else {
			FunctionFrame frame = FunctionFrame.createCall(function, params);
			vm.pushFrame(frame);
		}
	}
}
