package tinywasmr.engine.execution.stack;

import java.util.Stack;

import tinywasmr.engine.execution.exception.TrapException;
import tinywasmr.engine.execution.value.F32ValueHolder;
import tinywasmr.engine.execution.value.F64ValueHolder;
import tinywasmr.engine.execution.value.I32ValueHolder;
import tinywasmr.engine.execution.value.I64ValueHolder;
import tinywasmr.engine.execution.value.ValueHolder;

public class StackBackedMachineStack implements MachineStack {
	private Stack<ValueHolder> backed = new Stack<>();

	public Stack<ValueHolder> getBacked() { return backed; }

	@Override
	public void pushI32(int v) {
		backed.push(new I32ValueHolder(v));
	}

	@Override
	public int popI32() {
		if (backed.isEmpty()) throw new TrapException("Stack underflow");
		return backed.pop().getI32();
	}

	@Override
	public void pushI64(long v) {
		backed.push(new I64ValueHolder(v));
	}

	@Override
	public long popI64() {
		if (backed.isEmpty()) throw new TrapException("Stack underflow");
		return backed.pop().getI64();
	}

	@Override
	public void pushF32(float v) {
		backed.push(new F32ValueHolder(v));
	}

	@Override
	public float popF32() {
		if (backed.isEmpty()) throw new TrapException("Stack underflow");
		return backed.pop().getF32();
	}

	@Override
	public void pushF64(double v) {
		backed.push(new F64ValueHolder(v));
	}

	@Override
	public double popF64() {
		if (backed.isEmpty()) throw new TrapException("Stack underflow");
		return backed.pop().getF64();
	}

	@Override
	public void push(ValueHolder holder) {
		backed.push(holder.copy());
	}

	@Override
	public ValueHolder pop() {
		if (backed.isEmpty()) throw new TrapException("Stack underflow");
		return backed.pop();
	}
}
