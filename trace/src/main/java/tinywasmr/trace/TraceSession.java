package tinywasmr.trace;

import java.util.Collections;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tinywasmr.engine.execution.Executor;
import tinywasmr.engine.execution.ModuleInstance;
import tinywasmr.engine.execution.exception.TrapException;
import tinywasmr.engine.execution.stack.StackBackedMachineStack;
import tinywasmr.engine.execution.value.F32ValueHolder;
import tinywasmr.engine.execution.value.F64ValueHolder;
import tinywasmr.engine.execution.value.I32ValueHolder;
import tinywasmr.engine.execution.value.I64ValueHolder;
import tinywasmr.engine.execution.value.LocalsBuilder;

@Command(name = "<stdin>")
public class TraceSession {
	private Executor executor = null;
	private ModuleInstance instance;
	private MainCommand command;

	public TraceSession(MainCommand command, ModuleInstance instance) {
		this.command = command;
		this.instance = instance;
	}

	public ModuleInstance getInstance() { return instance; }

	public void sendCommand(String command) {
		if (command.isBlank()) step();
		else {
			new CommandLine(this).execute(command.split(" "));
			System.out.println("(trace) Ready.");
		}
	}

	@Command(aliases = { "exit", "bye", "quit" })
	public void exit() {
		System.out.println("(trace) Bye.");
		System.exit(0);
	}

	@Command
	public void step() {
		if (executor == null) {
			System.err.println("(trace) Nothing to step.");
			return;
		}

		try {
			if (!executor.step()) {
				if (command.autoInspect) inspect();
				else System.out.println("(trace) Stepped.");
			} else {
				if (executor.getContext().getStack() instanceof StackBackedMachineStack stack) {
					System.out.println("(trace) Values stack (final):");
					for (var e : stack.getBacked()) System.out.println("  :: " + e);
				}

				System.out.println("(trace) Finished.");
				executor = null;
			}
		} catch (TrapException e) {
			System.err.println("(trace) trap: " + e.getMessage());
			inspect();
			System.out.println("(trace) To push to values stack, use pushStack <i32/i64/f32/f64> <value>");
			System.out.println("(trace) To edit local, use editLocal <localIdx> <value>");
			System.out.println("(trace) To clear current function, use clear");
		}
	}

	@Command
	public void inspect() {
		if (executor == null) {
			System.err.println("(trace) Nothing to inspect.");
			return;
		}

		System.out.println("(trace) Code stack: " + executor.getStackEntries().size());
		for (var s : executor.getStackEntries()) {
			System.out.println("  " + s.getPointer() + " -> " + s.getCurrent());
			if ((s.getPointer() + 1) < s.getCode().size())
				System.out.println("  :::: " + s.getCode().get(s.getPointer() + 1));
		}

		if (executor.getContext().getStack() instanceof StackBackedMachineStack stack) {
			System.out.println("(trace) Values stack: " + stack.getBacked().size());
			for (var e : stack.getBacked()) System.out.println("  :: " + e);
		}

		System.out.println("(trace) Locals: " + executor.getContext().getLocals().size());
		for (int i = 0; i < executor.getContext().getLocals().size(); i++) {
			var local = executor.getContext().getLocals().get(i);
			System.out.println("  " + i + " -> " + local);
		}
	}

	@Command
	public void startTrace(int functionId, String... params) {
		if (executor != null) {
			System.err.println("(trace) Can't start trace: Already tracing.");
			return;
		}

		var imports = instance.getModule().getImportsSection()
			.map(v -> v.getImports().size())
			.orElse(0);
		var functions = instance.getModule().getFunctionsSection()
			.map(v -> v.getFunctions())
			.orElse(Collections.emptyList());

		if (functionId < imports) {
			System.err.println("(trace) Can't trace imported function.");
			return;
		}

		if (functionId >= (imports + functions.size())) {
			System.err.println("(trace) Function not found.");
			return;
		}

		var function = functions.get(functionId - imports);
		if (params.length != function.getSignature().getArgumentTypes().size()) {
			System.err.println("(trace) Parameters count mismatch.");
			return;
		}

		var locals = new LocalsBuilder();
		for (int i = 0; i < params.length; i++) {
			locals = switch (function.getSignature().getArgumentTypes().get(i).getTypeEnum()) {
			case I32 -> locals.i32(Integer.parseInt(params[i]));
			case I64 -> locals.i64(Long.parseLong(params[i]));
			case F32 -> locals.f32(Float.parseFloat(params[i]));
			case F64 -> locals.f64(Double.parseDouble(params[i]));
			default -> locals;
			};
		}

		var ctx = instance.newExecContext(function, locals);
		executor = new Executor(ctx);
		executor.pushCode(function.getCode().getInstructions());
		System.out.println("(trace) Function loaded.");
		inspect();
	}

	@Command
	public void editLocal(int localIdx, String value) {
		if (executor == null) {
			System.err.println("(trace) Nothing to edit.");
			return;
		}

		var locals = executor.getContext().getLocals();
		if (localIdx >= locals.size()) {
			System.err.println("(trace) Invalid local index.");
			return;
		}

		var local = locals.get(localIdx);
		if (local instanceof I32ValueHolder) local.setI32(Integer.parseInt(value));
		if (local instanceof I64ValueHolder) local.setI64(Long.parseLong(value));
		if (local instanceof F32ValueHolder) local.setF32(Float.parseFloat(value));
		if (local instanceof F64ValueHolder) local.setF64(Double.parseDouble(value));
	}

	@Command
	public void popStack() {
		if (executor == null) {
			System.err.println("(trace) Nothing to pop.");
			return;
		}

		try {
			executor.getContext().getStack().pop();
		} catch (TrapException e) {
			System.err.println("(trace) trap: " + e.getMessage());
		}
	}

	@Command
	public void pushStack(String type, String value) {
		if (executor == null) {
			System.err.println("(trace) Nothing to push.");
			return;
		}

		switch (type.toLowerCase()) {
		case "i32":
			executor.getContext().getStack().pushI32(Integer.parseInt(value));
			break;
		case "i64":
			executor.getContext().getStack().pushI64(Long.parseLong(value));
			break;
		case "f32":
			executor.getContext().getStack().pushF32(Float.parseFloat(value));
			break;
		case "f64":
			executor.getContext().getStack().pushF64(Double.parseDouble(value));
			break;
		}
	}

	@Command
	public void clear() {
		executor = null;
	}
}
