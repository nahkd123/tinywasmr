package tinywasmr.engine.exec.table;

import tinywasmr.engine.exec.instance.Exportable;
import tinywasmr.engine.exec.value.RefValue;
import tinywasmr.engine.module.table.TableDecl;
import tinywasmr.engine.type.TableType;
import tinywasmr.engine.type.value.RefType;

/**
 * <p>
 * A table is an array of opaque values of a particular element type. It allows
 * programs to select such values indirectly through a dynamic index operand.
 * Currently, the only available element type is an untyped function reference
 * or a reference to an external host value. Thereby, a program can call
 * functions indirectly through a dynamic index into a table. For example, this
 * allows emulating function pointers by way of table indices.
 * </p>
 * <p>
 * In other words, a table is an array of {@link RefValue} that have the same
 * {@link RefType}.
 * </p>
 * <p>
 * Depending on use case, you may create a new {@link DefaultTable} and manually
 * fill in the values, or implement this interface and have more control over
 * table instructions.
 * </p>
 */
public interface Table extends Exportable {
	TableDecl declaration();

	/**
	 * <p>
	 * The type of this table.
	 * </p>
	 */
	TableType type();

	/**
	 * <p>
	 * Return the element type of this table. Each element have this exact type.
	 * </p>
	 */
	default RefType refType() {
		return type().refType();
	}

	/**
	 * <p>
	 * The current size of this table. Depending on table type, the size can be
	 * changed. Usually tables declared from module can be resized, while external
	 * tables can't.
	 * </p>
	 */
	int size();

	/**
	 * <p>
	 * Get the element in this table. The returned value is never {@code null}, but
	 * the value that is wrapped by {@link RefType} (which is returned from
	 * {@link RefValue#get()}) might be {@code null}. This method behaves like
	 * {@code table.get} instruction.
	 * </p>
	 * 
	 * @param index The index in this table, starting from 0 and must smaller than
	 *              current {@link #size()}.
	 * @return The element at specified index in this table.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 */
	RefValue get(int index);

	/**
	 * <p>
	 * Set the element to this table. This method behaves like {@code table.set}
	 * instruction.
	 * </p>
	 * 
	 * @param index The index in this table, starting from 0 and must smaller than
	 *              current {@link #size()}.
	 * @param value The value to set to this table.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 * @throws IllegalArgumentException  if the provided value's {@link RefType}
	 *                                   does not equals to {@link #refType()}.
	 */
	void set(int index, RefValue value);

	/**
	 * <p>
	 * Fill the table with value. This method behaves like {@code table.fill}
	 * instruction.
	 * </p>
	 * 
	 * @param offset The starting index in the table to begin fill.
	 * @param value  The value to fill the entire table.
	 * @param count  The number of elements to fill.
	 * @throws IndexOutOfBoundsException if {@code offset + count} is larger than
	 *                                   the current size.
	 */
	default void fill(int offset, RefValue value, int count) {
		int size = size();
		if ((offset + count) > size) throw new IndexOutOfBoundsException();
		if (count == 0) return;
		for (int i = 0; i < count; i++) set(offset + i, value);
	}

	/**
	 * <p>
	 * Grow (or shrink) this table and return the previous size. This method behaves
	 * like {@code table.grow} instruction.
	 * </p>
	 * 
	 * @param delta The number of elements to increase or decrease the size.
	 *              Positive value indicates increasing the size.
	 * @param value The value to fill in the area that is grown by this method.
	 * @return The previous size of this table, or {@code -1} if the table cannot
	 *         grow.
	 */
	int grow(int delta, RefValue value);
}
