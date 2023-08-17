package tinywasmr.engine.execution;

import java.util.List;
import java.util.Stack;

import tinywasmr.engine.execution.context.ExecutionContext;
import tinywasmr.engine.execution.exception.TrapException;
import tinywasmr.engine.instruction.Instruction;
import tinywasmr.engine.instruction.special.BlockInstruction;

public class Executor {
	public static class StackEntry {
		private List<Instruction> code;
		private int pointer;

		public StackEntry(List<Instruction> code, int pointer) {
			this.code = code;
			this.pointer = pointer;
		}

		public Instruction getCurrent() { return code.get(pointer); }

		public boolean isDone() { return pointer >= code.size(); }

		public List<Instruction> getCode() { return code; }

		public int getPointer() { return pointer; }
	}

	private Stack<StackEntry> entries = new Stack<>();
	private ExecutionContext context;

	public Executor(ExecutionContext context) {
		this.context = context;
	}

	public ExecutionContext getContext() { return context; }

	public void pushCode(List<Instruction> code) {
		entries.push(new StackEntry(code, 0));
	}

	public boolean step() throws TrapException {
		StackEntry tail;
		while ((tail = entries.lastElement()).isDone()) entries.pop();
		if (entries.isEmpty()) return true;

		var insn = tail.getCurrent();
		if (insn instanceof BlockInstruction block) {
			var body = block.getBodyBasedOnContext(context);
			if (body.isEmpty()) return false; // continue

			pushCode(body.get());
			return false;
		} else {
			insn.execute(context);

			if (context.isReturned()) {
				entries.clear();
				return true;
			}

			while (context.getBranchOutDepth() > 0) {
				context.setBranchOutDepth(context.getBranchOutDepth() - 1);
				entries.pop();
			}
		}

		tail.pointer++;

		if (tail.isDone()) {
			entries.pop();
			if (entries.isEmpty()) return true;
		}

		return false;
	}

	public Stack<StackEntry> getStackEntries() { return entries; }
}
