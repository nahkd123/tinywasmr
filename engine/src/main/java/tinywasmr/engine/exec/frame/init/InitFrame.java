package tinywasmr.engine.exec.frame.init;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tinywasmr.engine.exec.frame.AbstractFrame;
import tinywasmr.engine.exec.frame.InstancedFrame;
import tinywasmr.engine.exec.instance.Function;
import tinywasmr.engine.exec.instance.Instance;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.module.global.ModuleGlobalDecl;
import tinywasmr.engine.module.memory.ActiveDataMode;
import tinywasmr.engine.module.memory.DataSegment;
import tinywasmr.engine.module.table.ActiveElementMode;
import tinywasmr.engine.module.table.ElementSegment;
import tinywasmr.engine.type.BlockType;
import tinywasmr.engine.type.ResultType;

/**
 * <p>
 * Init frame that is used to initialize an instance. When you create an
 * instance through constructor, you don't use the instance right away; if the
 * instance have globals, passive element memory segments, you need to
 * initialize the instance.
 * </p>
 */
public class InitFrame extends AbstractFrame implements InstancedFrame {
	private Instance instance;
	private List<ModuleGlobalDecl> globals = new ArrayList<>();
	private List<DataSegment> activeData = new ArrayList<>();
	private List<ElementSegment> activeElement = new ArrayList<>();
	private Function startFunction;

	public InitFrame(Instance instance, List<Value> operands, int step) {
		super(operands, step);
		this.instance = instance;

		if (instance.module() != null) {
			globals.addAll(instance.module().declaredGlobals().stream()
				.filter(v -> v instanceof ModuleGlobalDecl)
				.map(v -> (ModuleGlobalDecl) v)
				.toList());
			activeData.addAll(instance.module().dataSegments().stream()
				.filter(segment -> segment.mode() instanceof ActiveDataMode)
				.toList());
			activeElement.addAll(instance.module().elementSegments().stream()
				.filter(segment -> segment.mode() instanceof ActiveElementMode)
				.toList());
			startFunction = instance.module().startFunction() != null
				? new Function(instance, instance.module().startFunction())
				: null;
		}
	}

	public InitFrame(Instance instance) {
		this(instance, Collections.emptyList(), 0);
	}

	@Override
	public boolean isFrameFinished() {
		return getStep() >= (globals.size() + activeData.size() + activeElement.size());
	}

	public void executeStep(Machine vm, int step) {
		if (step < globals.size()) {
			ModuleGlobalDecl decl = globals.get(step);
			vm.pushFrame(new InitGlobalFrame(decl));
			return;
		}

		step -= globals.size();
		if (step < activeData.size()) {
			DataSegment segment = activeData.get(step);
			vm.pushFrame(new DataSegmentInitFrame(segment));
			return;
		}

		step -= activeData.size();
		if (step < activeElement.size()) {
			ElementSegment segment = activeElement.get(step);
			vm.pushFrame(new ElementSegmentInitFrame(segment));
			return;
		}

		step -= activeElement.size();
		if (step == 0) {
			vm.call(startFunction, new Value[0]);
			return;
		}
	}

	@Override
	public void executeStep(Machine vm) {
		executeStep(vm, getStep());
	}

	@Override
	public void branchThis() {
		throw new IllegalCallerException("Branching init frame is not allowed");
	}

	@Override
	public BlockType getBranchResultTypes() { return new ResultType(Collections.emptyList()); }

	@Override
	public Instance getInstance() { return instance; }
}
