package tinywasmr.engine.insn.control;

import java.util.List;

import tinywasmr.engine.exec.ValidationException;
import tinywasmr.engine.exec.instance.Function;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.type.value.ValueType;

public record CallInsn(FunctionDecl function) implements Instruction {
	@Override
	public void execute(Machine vm) {
		Function function = vm.peekFunctionFrame().getInstance().function(this.function);
		callFunction(vm, function);
	}

	public static void callFunction(Machine vm, Function function) {
		List<ValueType> paramTypes = function.type().inputs().types();
		Value[] params = new Value[paramTypes.size()];

		for (int i = params.length - 1; i >= 0; i--) {
			Value val = vm.peekFrame().popOprand();

			if (vm.hasRuntimeValidation() && !val.type().equals(paramTypes.get(i)))
				throw new ValidationException("Type mismatch: %s (stack) != %s (declared)"
					.formatted(val.type(), paramTypes.get(i)));

			params[i] = val;
		}

		vm.call(function, params);
	}
}
