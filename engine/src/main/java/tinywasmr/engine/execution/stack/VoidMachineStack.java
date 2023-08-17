package tinywasmr.engine.execution.stack;

import tinywasmr.engine.execution.value.I32ValueHolder;
import tinywasmr.engine.execution.value.ValueHolder;

/**
 * <p>
 * A machine stack that doesn't do anything. It discards values when push and
 * returns zero when pop.
 * </p>
 */
public class VoidMachineStack implements MachineStack {
	public static final VoidMachineStack STACK = new VoidMachineStack();

	private VoidMachineStack() {}

	@Override
	public void pushI32(int v) {}

	@Override
	public int popI32() {
		return 0;
	}

	@Override
	public void pushI64(long v) {}

	@Override
	public long popI64() {
		return 0;
	}

	@Override
	public void pushF32(float v) {}

	@Override
	public float popF32() {
		return 0;
	}

	@Override
	public void pushF64(double v) {}

	@Override
	public double popF64() {
		return 0;
	}

	@Override
	public void push(ValueHolder holder) {}

	@Override
	public ValueHolder pop() {
		return new I32ValueHolder(0);
	}
}
