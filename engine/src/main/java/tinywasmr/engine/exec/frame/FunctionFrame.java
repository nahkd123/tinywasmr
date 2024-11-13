package tinywasmr.engine.exec.frame;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.instance.Function;
import tinywasmr.engine.exec.instance.Instance;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.func.ModuleFunctionDecl;
import tinywasmr.engine.module.func.extern.ExternalFunctionDecl;
import tinywasmr.engine.type.BlockType;
import tinywasmr.engine.type.value.ValueType;

public class FunctionFrame extends AbstractFrame implements InstancedFrame {
	private Function function;
	private Value[] locals;
	private boolean branched = false;

	public FunctionFrame(Function function, Value[] locals, boolean branched, List<Value> operands, int insn) {
		super(operands, insn);
		this.function = function;
		this.locals = locals;
		this.branched = branched;
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

		FunctionFrame out = new FunctionFrame(function, new Value[allLocals.size()], false, Collections.emptyList(), 0);
		for (int i = 0; i < parameters.length; i++) out.locals[i] = parameters[i];
		for (int i = 0; i < extras.size(); i++) out.locals[parameters.length + i] = extras.get(i).zero();
		return out;
	}

	public Function getFunction() { return function; }

	public FunctionDecl getDeclaration() { return function.declaration(); }

	@Override
	public Instance getInstance() { return function.instance(); }

	/**
	 * <p>
	 * Get all local variables. The local variables are ordered from function
	 * parameters first to explicitly declared variables.
	 * </p>
	 */
	public Value[] getLocals() { return locals; }

	@Override
	public boolean isFrameFinished() {
		// TODO Improve external function handling
		if (function.declaration() instanceof ExternalFunctionDecl) return branched;
		if (function.declaration() instanceof ModuleFunctionDecl module) return getStep() >= module.body().size();
		return true;
	}

	public boolean isBranched() { return branched; }

	@Override
	public void branchThis() {
		branched = true;
		if (function.declaration() instanceof ModuleFunctionDecl module) setStep(module.body().size());
	}

	@Override
	public BlockType getBranchResultTypes() { return function.type().outputs(); }

	@Override
	public void executeStep(Machine vm) {
		if (function.declaration() instanceof ExternalFunctionDecl extern) {
			extern.onStep(vm, this, locals, getStep());
			return;
		}

		if (function.declaration() instanceof ModuleFunctionDecl module) {
			if (getStep() < module.body().size()) module.body().get(getStep()).execute(vm);
			return;
		}
	}
}
