package tinywasmr.engine.module.func;

import java.util.List;
import java.util.stream.Stream;

import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.type.FunctionType;
import tinywasmr.engine.type.value.ValueType;

public class ModuleFunctionDecl implements FunctionDecl {
	private WasmModule module;
	private FunctionType type;
	private List<ValueType> extraLocals;
	private List<Instruction> body;

	public ModuleFunctionDecl(WasmModule module, FunctionType type, List<ValueType> extraLocals, List<Instruction> body) {
		this.module = module;
		this.type = type;
		this.extraLocals = extraLocals;
		this.body = body;
	}

	public WasmModule module() {
		return module;
	}

	@Override
	public FunctionType type() {
		return type;
	}

	public List<ValueType> extraLocals() {
		return extraLocals;
	}

	public List<Instruction> body() {
		return body;
	}

	/**
	 * <p>
	 * Get all local types, ordered from function parameters to declared extra
	 * locals.
	 * </p>
	 */
	public List<ValueType> allLocals() {
		return Stream.concat(type.inputs().types().stream(), extraLocals.stream()).toList();
	}
}
