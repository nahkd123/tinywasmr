package tinywasmr.engine.exec.executor;

import java.util.List;

import tinywasmr.engine.exec.StepResult;
import tinywasmr.engine.exec.ValidationException;
import tinywasmr.engine.exec.frame.BlockFrame;
import tinywasmr.engine.exec.frame.Frame;
import tinywasmr.engine.exec.frame.FunctionFrame;
import tinywasmr.engine.exec.frame.TableInitFrame;
import tinywasmr.engine.exec.trap.ExternalTrap;
import tinywasmr.engine.exec.value.RefValue;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.insn.control.BlockInsn;
import tinywasmr.engine.module.func.extern.ExternalFunctionDecl;
import tinywasmr.engine.type.value.ValueType;

/**
 * <p>
 * The default implementation of {@link Executor}.
 * </p>
 */
public class DefaultExecutor implements Executor {
	private boolean shouldPopFrame(Machine vm) {
		if (vm.peekFrame() instanceof TableInitFrame init) {
			if (init.getInsnIndex() < init.getCount()) return false;
			return true;
		}

		if (vm.peekFrame() instanceof FunctionFrame func) {
			if (func.getDeclaration() instanceof ExternalFunctionDecl) return false;
			//
		}

		return vm.peekFrame().getInsnIndex() >= vm.peekFrame().getExecutingInsns().size();
	}

	@Override
	public StepResult step(Machine vm) {
		if (vm.getTrap() != null) return StepResult.TRAP;
		if (vm.peekFrame() == vm.getExternalFrame()) {
			vm.setTrap(new ExternalTrap(new IllegalStateException("Can't step in external frame")));
			return StepResult.TRAP;
		}

		while (shouldPopFrame(vm)) {
			if (vm.peekFrame() instanceof TableInitFrame init) initTableElement(init);
			List<ValueType> resultTypes = vm.peekFrame().getBranchResultTypes().blockResults();
			Value[] results = new Value[resultTypes.size()];

			for (int i = results.length - 1; i >= 0; i--) {
				Value val = vm.peekFrame().popOprand();

				if (vm.hasRuntimeValidation() && !val.type().equals(resultTypes.get(i))) {
					vm.setTrap(new ExternalTrap(new ValidationException("Type mismatch: %s (stack) != %s (bresult)"
						.formatted(val.type(), resultTypes.get(i)))));
					return StepResult.TRAP;
				}

				results[i] = val;
			}

			vm.popFrame();
			for (Value val : results) vm.peekFrame().pushOperand(val);
			if (vm.peekFrame() == vm.getExternalFrame()) return StepResult.NORMAL;
		}

		Frame frame = vm.peekFrame();

		try {
			if (frame instanceof FunctionFrame functionFrame
				&& functionFrame.getDeclaration() instanceof ExternalFunctionDecl extern) {
				try {
					extern.onStep(vm, functionFrame, functionFrame.getLocals(), functionFrame.getInsnIndex());
				} catch (Throwable t) {
					vm.setTrap(new ExternalTrap(t));
					return StepResult.TRAP;
				}
			} else if (frame instanceof TableInitFrame init) {
				if (init.getInsnIndex() > 0)
					try {
						initTableElement(init);
					} catch (Throwable t) {
						vm.setTrap(new ExternalTrap(t));
						return StepResult.TRAP;
					}

				BlockInsn expr = init.getSegment().init().get(init.getSegmentOffset() + init.getInsnIndex());
				vm.pushFrame(new BlockFrame(expr));
			} else {
				frame.getExecutingInsns().get(frame.getInsnIndex()).execute(vm);
			}

			return StepResult.NORMAL;
		} catch (Throwable e) {
			vm.setTrap(new ExternalTrap(e));
			return StepResult.TRAP;
		} finally {
			frame.incInsnIndex();
		}
	}

	private void initTableElement(TableInitFrame init) {
		Value val = init.popOprand();
		if (!(val instanceof RefValue ref)) throw new ValidationException("Expected reftype, found %s".formatted(val));
		init.getTable().set(init.getTableOffset() + init.getInsnIndex() - 1, ref);
	}
}
