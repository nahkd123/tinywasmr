package tinywasmr.extern;

import java.lang.reflect.Field;

import tinywasmr.engine.exec.memory.ByteArrayMemoryView;
import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.engine.module.memory.MemoryDecl;
import tinywasmr.engine.type.Limit;
import tinywasmr.engine.type.MemoryType;

public class ReflectedMemoryDecl implements MemoryDecl {
	private Field field;

	public ReflectedMemoryDecl(Field field) {
		this.field = field;
	}

	public Field field() {
		return field;
	}

	@Override
	public MemoryType type() {
		return new MemoryType(new Limit(1));
	}

	public Memory getFrom(Object instance) {
		try {
			field.setAccessible(true);
			Object result = field.get(instance);
			field.setAccessible(false);

			if (result instanceof Memory mem) return mem;
			if (result instanceof byte[] bs) return new ByteArrayMemoryView(this, bs);
			throw new IllegalArgumentException("Unable to convert %s to Memory".formatted(result));
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
