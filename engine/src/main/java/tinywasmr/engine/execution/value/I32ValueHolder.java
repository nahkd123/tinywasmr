package tinywasmr.engine.execution.value;

public final class I32ValueHolder implements ValueHolder {
	private int v;

	public I32ValueHolder(int v) {
		this.v = v;
	}

	@Override
	public int getI32() { return v; }

	@Override
	public void setI32(int v) { this.v = v; }

	@Override
	public long getI64() { return v; }

	@Override
	public void setI64(long v) { this.v = (int) v; }

	@Override
	public float getF32() { return v; }

	@Override
	public void setF32(float v) { this.v = (int) v; }

	@Override
	public double getF64() { return v; }

	@Override
	public void setF64(double v) { this.v = (int) v; }

	@Override
	public ValueHolder copy() {
		return new I32ValueHolder(v);
	}

	@Override
	public void copyTo(ValueHolder holder) {
		holder.setI32(v);
	}
}
