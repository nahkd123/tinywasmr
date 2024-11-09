package tinywasmr.engine.exec.global;

import tinywasmr.engine.exec.instance.Exportable;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.module.global.GlobalDecl;
import tinywasmr.engine.type.Mutability;
import tinywasmr.engine.type.value.ValueType;

public interface Global extends Exportable {
	GlobalDecl declaration();

	default Mutability mutability() {
		return declaration().type().mutablity();
	}

	default ValueType type() {
		return declaration().type().valType();
	}

	Value get();

	/**
	 * <p>
	 * Set the global variable with different value.
	 * </p>
	 * 
	 * @param value              The value to set.
	 * @param ignoreImmutability Whether to attempt to ignore the restriction of
	 *                           {@link #mutability()}.
	 */
	void set(Value value, boolean ignoreImmutability);

	default void set(Value value) {
		set(value, false);
	}
}
