package tinywasmr.engine.execution;

import tinywasmr.engine.execution.context.SimpleExecutionContext;
import tinywasmr.engine.execution.value.LocalsBuilder;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.function.Function;

public class ModuleInstance {
	private WasmModule module;

	public ModuleInstance(WasmModule module) {
		this.module = module;
	}

	public WasmModule getModule() { return module; }

	public SimpleExecutionContext newExecContext(Function function, LocalsBuilder params) {
		var currentParams = params.build().size();
		var requiredParams = function.getSignature().getArgumentTypes().size();
		if (currentParams < requiredParams)
			throw new IllegalArgumentException("Missing parameters (" + currentParams + "/" + requiredParams + ")");

		for (var l : function.getCode().getLocals()) {
			params = switch (l.getTypeEnum()) {
			case I32 -> params.i32();
			case I64 -> params.i64();
			case F32 -> params.f32();
			case F64 -> params.f64();
			default -> throw new RuntimeException("Unimplemented parameter type: " + l.getTypeEnum());
			};
		}

		return new SimpleExecutionContext(this, params);
	}
}
