package tinywasmr.engine.exec.instance;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.engine.exec.table.Table;
import tinywasmr.engine.module.func.ExternalFunctionDecl;
import tinywasmr.engine.type.value.ValueType;

/**
 * <p>
 * A simple implementation of {@link Importer}. The key of the map follows
 * {@code <module>::<name>} format, and the value can be any of:
 * <ul>
 * <li>- {@link Function}: Can be from other module or external function</li>
 * <li>- {@link ExternalFunctionDecl}: External function without instance</li>
 * <li>- {@link Runnable}: {@code void()} function without instance</li>
 * <li>- {@code byte[]}: Memory</li>
 * </ul>
 * </p>
 */
public record SimpleImporter(Map<String, Object> objects) implements Importer {

	public static String key(String module, String name) {
		return "%s::%s".formatted(module, name);
	}

	@SafeVarargs
	public static SimpleImporter of(Map.Entry<String, Object>... entries) {
		return new SimpleImporter(Map.ofEntries(entries));
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public Function importFunction(String module, String name) {
		Object obj = objects.get(key(module, name));
		if (obj instanceof Function function) return function;
		if (obj instanceof ExternalFunctionDecl extern) return new Function(null, extern);
		if (obj instanceof Runnable runnable) return new Function(null, ExternalFunctionDecl.ofVoid(runnable));
		return null;
	}

	public Table importTable(String module, String name) {
		Object obj = objects.get(key(module, name));
		if (obj instanceof Table table) return table;
		// TODO TableView - A view to RefValue[]
		return null;
	}

	@Override
	public Memory importMemory(String module, String name) {
		Object obj = objects.get(key(module, name));
		if (obj instanceof Memory memory) return memory;
		// TODO MemoryView - A view to byte[]
		return null;
	}

	public static class Builder {
		private Map<String, Object> map = new HashMap<>();

		public SimpleImporter build() {
			return new SimpleImporter(map);
		}

		public Builder add(String module, String name, Object value) {
			map.put(key(module, name), value);
			return this;
		}

		public Builder module(String name, Consumer<ModuleBuilder> callback) {
			callback.accept(new ModuleBuilder(this, name));
			return this;
		}
	}

	public static class ModuleBuilder {
		private Builder parent;
		private String name;

		ModuleBuilder(Builder parent, String name) {
			this.parent = parent;
			this.name = name;
		}

		public ModuleBuilder add(String name, Object value) {
			parent.add(this.name, name, value);
			return this;
		}

		public ModuleBuilder addVoidFunc(String name, Runnable runnable) {
			return add(name, ExternalFunctionDecl.ofVoid(runnable));
		}

		public <P1> ModuleBuilder addVoidFunc(String name, ValueType p1, Consumer<? extends P1> consumer) {
			return add(name, ExternalFunctionDecl.ofVoid(p1, consumer));
		}

		public <P1, P2> ModuleBuilder addVoidFunc(String name, ValueType p1, ValueType p2, BiConsumer<? extends P1, ? extends P2> consumer) {
			return add(name, ExternalFunctionDecl.ofVoid(p1, p2, consumer));
		}

		public <R> ModuleBuilder addFunc(String name, ValueType ret, Supplier<? extends R> supplier) {
			return add(name, ExternalFunctionDecl.of(ret, supplier));
		}

		public <R, P1> ModuleBuilder addFunc(String name, ValueType ret, ValueType p1, java.util.function.Function<? extends P1, ? extends R> supplier) {
			return add(name, ExternalFunctionDecl.of(ret, p1, supplier));
		}

		public <R, P1, P2> ModuleBuilder addFunc(String name, ValueType ret, ValueType p1, ValueType p2, BiFunction<? extends P1, ? extends P2, ? extends R> supplier) {
			return add(name, ExternalFunctionDecl.of(ret, p1, p2, supplier));
		}
	}
}
