package tinywasmr.engine.exec.global;

import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.module.global.GlobalDecl;
import tinywasmr.engine.type.Mutability;
import tinywasmr.engine.type.value.ValueType;

public class DefaultGlobal implements Global {
	private GlobalDecl declaration;
	private Mutability mutability;
	private ValueType valType;
	private Value value;

	public DefaultGlobal(GlobalDecl declaration, Value init) {
		this.declaration = declaration;
		this.mutability = declaration.type().mutablity();
		this.valType = declaration.type().valType();
		this.value = init;
	}

	public DefaultGlobal(Mutability mutability, ValueType type, Value init) {
		this.mutability = mutability;
		this.valType = type;
		this.value = init;
	}

	@Override
	public GlobalDecl declaration() {
		return declaration;
	}

	@Override
	public Mutability mutability() {
		return mutability;
	}

	@Override
	public ValueType type() {
		return valType;
	}

	@Override
	public Value get() {
		if (value == null) throw new IllegalStateException("The global must be initialized before getting");
		return value;
	}

	@Override
	public void set(Value value, boolean ignoreImmutability) {
		if (!value.type().equals(valType)) throw new IllegalArgumentException("Type mismatch: %s != %s (declared)"
			.formatted(value.type(), valType));

		if (this.value == null || mutability == Mutability.VAR || ignoreImmutability) {
			this.value = value;
			return;
		}

		throw new IllegalStateException("This global is immutable");
	}
}
