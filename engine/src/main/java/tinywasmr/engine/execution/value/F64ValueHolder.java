package tinywasmr.engine.execution.value;

public final class F64ValueHolder implements ValueHolder {
	private double v;

	public F64ValueHolder(double v) {
		this.v = v;
	}

	@Override
	public int getI32() { return (int) v; }

	@Override
	public void setI32(int v) { this.v = v; }

	@Override
	public long getI64() { return (long) v; }

	@Override
	public void setI64(long v) { this.v = v; }

	@Override
	public float getF32() { return (float) v; }

	@Override
	public void setF32(float v) { this.v = v; }

	@Override
	public double getF64() { return v; }

	@Override
	public void setF64(double v) { this.v = v; }

	@Override
	public ValueHolder copy() {
		return new F64ValueHolder(v);
	}

	@Override
	public void copyTo(ValueHolder holder) {
		holder.setF64(v);
	}
}
