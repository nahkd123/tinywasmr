package tinywasmr.engine.execution.value;

import java.util.ArrayList;
import java.util.List;

public final class LocalsBuilder {
	private List<ValueHolder> holders = new ArrayList<>();

	public LocalsBuilder i32(int v) {
		holders.add(new I32ValueHolder(v));
		return this;
	}

	public LocalsBuilder i32() {
		return i32(0);
	}

	public LocalsBuilder i64(long v) {
		holders.add(new I64ValueHolder(v));
		return this;
	}

	public LocalsBuilder i64() {
		return i64(0);
	}

	public LocalsBuilder f32(float v) {
		holders.add(new F32ValueHolder(v));
		return this;
	}

	public LocalsBuilder f32() {
		return f32(0f);
	}

	public LocalsBuilder f64(double v) {
		holders.add(new F64ValueHolder(v));
		return this;
	}

	public LocalsBuilder f64() {
		return f64(0d);
	}

	public List<ValueHolder> build() {
		return holders;
	}
}
