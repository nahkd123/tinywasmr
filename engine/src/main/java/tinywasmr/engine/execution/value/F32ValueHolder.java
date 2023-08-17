package tinywasmr.engine.execution.value;

public final class F32ValueHolder implements ValueHolder {
	private float v;

	public F32ValueHolder(float v) {
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
	public float getF32() { return v; }

	@Override
	public void setF32(float v) { this.v = v; }

	@Override
	public double getF64() { return v; }

	@Override
	public void setF64(double v) { this.v = (float) v; }

	@Override
	public ValueHolder copy() {
		return new F32ValueHolder(v);
	}

	@Override
	public void copyTo(ValueHolder holder) {
		holder.setF32(v);
	}
}
