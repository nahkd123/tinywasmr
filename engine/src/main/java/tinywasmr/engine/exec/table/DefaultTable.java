package tinywasmr.engine.exec.table;

import tinywasmr.engine.exec.ValidationException;
import tinywasmr.engine.exec.value.RefValue;
import tinywasmr.engine.module.table.TableDecl;
import tinywasmr.engine.type.TableType;
import tinywasmr.engine.type.value.RefType;

public class DefaultTable implements Table {
	private TableDecl declaration;
	private TableType type;
	private RefType refType;
	private RefValue[] values;

	public DefaultTable(TableDecl declaration, int size) {
		this.declaration = declaration;
		this.type = declaration.type();
		this.refType = type.refType();
		this.values = new RefValue[size];
	}

	public DefaultTable(TableDecl declaration) {
		this(declaration, declaration.type().limit().allocSize());
	}

	@Override
	public TableDecl declaration() {
		return declaration;
	}

	@Override
	public TableType type() {
		return type;
	}

	@Override
	public RefType refType() {
		return refType;
	}

	@Override
	public int size() {
		return values.length;
	}

	@Override
	public RefValue get(int index) {
		RefValue val = values[index];
		return val == null ? refType().zero() : val;
	}

	@Override
	public void set(int index, RefValue value) {
		if (!value.type().equals(refType))
			throw new ValidationException("Element type mismatch: %s (input) != %s (declared)"
				.formatted(value.type(), refType));
		values[index] = value.get() != null ? value : null;
	}

	@Override
	public int grow(int delta, RefValue value) {
		if (value.type().equals(refType))
			throw new ValidationException("Element type mismatch: %s (input) != %s (declared)"
				.formatted(value.type(), refType));

		if ((values.length + delta) < 0) return -1;
		int prev = values.length;
		RefValue[] newValues = new RefValue[values.length + delta];
		System.arraycopy(values, 0, newValues, 0, prev);
		if (value.get() != null && delta > 0) for (int i = 0; i < delta; i++) newValues[prev + i] = value;
		values = newValues;
		return prev;
	}
}
