package tinywasmr.engine.execution.value;

public final class I64ValueHolder implements ValueHolder {
	private long v;

	public I64ValueHolder(long v) {
		this.v = v;
	}

	@Override
	public int getI32() { return (int) (v & 0xFFFFFFFFL); }

	@Override
	public void setI32(int v) { this.v = v; }

	@Override
	public long getI64() { return v; }

	@Override
	public void setI64(long v) { this.v = v; }

	@Override
	public float getF32() { return v; }

	@Override
	public void setF32(float v) { this.v = (long) v; }

	@Override
	public double getF64() { return v; }

	@Override
	public void setF64(double v) { this.v = (long) v; }

	@Override
	public ValueHolder copy() {
		return new I64ValueHolder(v);
	}

	@Override
	public void copyTo(ValueHolder holder) {
		holder.setI64(v);
	}

	@Override
	public String toString() {
		return "i64<" + v + ">";
	}
}
