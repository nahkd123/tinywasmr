package tinywasmr.engine.exec.frame.init;

import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.frame.AbstractFrame;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.variable.GlobalInsnType;
import tinywasmr.engine.module.global.ModuleGlobalDecl;
import tinywasmr.engine.type.BlockType;
import tinywasmr.engine.type.ResultType;

public class InitGlobalFrame extends AbstractFrame {
	private ModuleGlobalDecl declaration;

	public InitGlobalFrame(ModuleGlobalDecl declaration, List<Value> operands, int step) {
		super(operands, step);
		this.declaration = declaration;
	}

	public InitGlobalFrame(ModuleGlobalDecl declaration) {
		this(declaration, Collections.emptyList(), 0);
	}

	public ModuleGlobalDecl getDeclaration() { return declaration; }

	@Override
	public boolean isFrameFinished() { return getStep() >= declaration.init().size() + 1; }

	@Override
	public void branchThis() {
		setStep(declaration.init().size()); // Jump to set insn instead of end of expression
	}

	@Override
	public BlockType getBranchResultTypes() { return new ResultType(Collections.emptyList()); }

	@Override
	public void executeStep(Machine vm) {
		if (isFrameFinished()) return;
		if (getStep() == declaration.init().size()) GlobalInsnType.SET.execute(vm, declaration);
		else declaration.init().get(getStep()).execute(vm);
	}
}
