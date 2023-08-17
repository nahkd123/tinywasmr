package tinywasmr.engine.execution.stack;

import tinywasmr.engine.execution.value.ValueHolder;

/**
 * <p>
 * A stack that can be used to push and pop values.
 * </p>
 */
public interface MachineStack {
	public void pushI32(int v);

	public int popI32();

	public void pushI64(long v);

	public long popI64();

	public void pushF32(float v);

	public float popF32();

	public void pushF64(double v);

	public double popF64();

	public void push(ValueHolder holder);

	public ValueHolder pop();
}
