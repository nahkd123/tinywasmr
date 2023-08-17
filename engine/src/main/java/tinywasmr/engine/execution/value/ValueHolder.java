package tinywasmr.engine.execution.value;

public sealed interface ValueHolder permits I32ValueHolder, I64ValueHolder, F32ValueHolder, F64ValueHolder {
	public int getI32();

	public void setI32(int v);

	public long getI64();

	public void setI64(long v);

	public float getF32();

	public void setF32(float v);

	public double getF64();

	public void setF64(double v);

	public ValueHolder copy();

	public void copyTo(ValueHolder holder);
}
