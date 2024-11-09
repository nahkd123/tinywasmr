package tinywasmr.engine.exec.frame;

import java.util.List;

import tinywasmr.engine.exec.instance.Function;
import tinywasmr.engine.exec.instance.Instance;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.insn.Instruction;
import tinywasmr.engine.module.func.ModuleFunctionDecl;
import tinywasmr.engine.type.BlockType;
import tinywasmr.engine.type.value.ValueType;

public class FunctionFrame extends AbstractFrame {
	private Function function;
	private ModuleFunctionDecl declaration;
	private Value[] locals;

	public FunctionFrame(Function function, List<Value> operands, Value[] locals, int insn) {
		super(operands, insn);
		if (!(function.declaration() instanceof ModuleFunctionDecl moduleDecl))
			throw new IllegalArgumentException("Cannot create function frame of non-module function");
		this.function = function;
		this.declaration = moduleDecl;
		this.locals = locals;
	}

	public static FunctionFrame createCall(Function function, Value[] parameters) {
		if (!(function.declaration() instanceof ModuleFunctionDecl moduleDecl))
			throw new IllegalArgumentException("Cannot create function frame of %s"
				.formatted(function.declaration().getClass().getName()));

		List<ValueType> allLocals = moduleDecl.allLocals();
		List<ValueType> extras = moduleDecl.extraLocals();
		FunctionFrame out = new FunctionFrame(function, List.of(), new Value[allLocals.size()], 0);
		for (int i = 0; i < parameters.length; i++) out.locals[i] = parameters[i];
		for (int i = 0; i < extras.size(); i++) out.locals[parameters.length + i] = extras.get(i).zero();
		return out;
	}

	public Function getFunction() { return function; }

	public ModuleFunctionDecl getDeclaration() { return declaration; }

	public Instance getInstance() { return function.instance(); }

	@Override
	public List<Instruction> getExecutingInsns() { return declaration.body(); }

	/**
	 * <p>
	 * Get all local variables. The local variables are ordered from function
	 * parameters first to explicitly declared variables.
	 * </p>
	 */
	public Value[] getLocals() { return locals; }

	@Override
	public BlockType getBranchResultTypes() { return function.type().outputs(); }

	@Override
	public String toString() {
		return "Function %s %s insn %04d -> %s".formatted(
			function,
			getOperandStack(),
			getInsnIndex(),
			getInsnIndex() < getExecutingInsns().size() ? getExecutingInsns().get(getInsnIndex()) : "<end of block>");
	}
}
