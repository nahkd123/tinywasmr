package tinywasmr.engine.exec.frame;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.instance.Function;
import tinywasmr.engine.exec.instance.Instance;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.func.ModuleFunctionDecl;
import tinywasmr.engine.module.func.extern.ExternalFunctionDecl;
import tinywasmr.engine.type.BlockType;
import tinywasmr.engine.type.value.ValueType;

public class FunctionFrame extends AbstractFrame {
	private Function function;
	private Value[] locals;

	public FunctionFrame(Function function, List<Value> operands, Value[] locals, int insn) {
		super(operands, insn);
		this.function = function;
		this.locals = locals;
	}

	public static FunctionFrame createCall(Function function, Value[] parameters) {
		List<ValueType> allLocals;
		List<ValueType> extras;

		if (function.declaration() instanceof ModuleFunctionDecl moduleDecl) {
			allLocals = moduleDecl.allLocals();
			extras = moduleDecl.extraLocals();
		} else if (function.declaration() instanceof ExternalFunctionDecl externDecl) {
			allLocals = externDecl.allLocals();
			extras = externDecl.localVariables();
		} else throw new IllegalArgumentException("Cannot create function frame of %s".formatted(
			function.declaration().getClass().getName()));

		FunctionFrame out = new FunctionFrame(function, List.of(), new Value[allLocals.size()], 0);
		for (int i = 0; i < parameters.length; i++) out.locals[i] = parameters[i];
		for (int i = 0; i < extras.size(); i++) out.locals[parameters.length + i] = extras.get(i).zero();
		return out;
	}

	public Function getFunction() { return function; }

	public FunctionDecl getDeclaration() { return function.declaration(); }

	public Instance getInstance() { return function.instance(); }

	@Override
	public List<Instruction> getExecutingInsns() {
		return function.declaration() instanceof ModuleFunctionDecl moduleDecl
			? moduleDecl.body()
			: Collections.emptyList();
	}

	/**
	 * <p>
	 * Get all local variables. The local variables are ordered from function
	 * parameters first to explicitly declared variables.
	 * </p>
	 */
	public Value[] getLocals() { return locals; }

	@Override
	public BlockType getBranchResultTypes() { return function.type().outputs(); }
}
