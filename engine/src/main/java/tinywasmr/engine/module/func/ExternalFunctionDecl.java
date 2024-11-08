package tinywasmr.engine.module.func;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import tinywasmr.engine.exec.instance.ExternalInstance;
import tinywasmr.engine.exec.instance.Instance;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.type.FunctionType;
import tinywasmr.engine.type.ResultType;
import tinywasmr.engine.type.value.ValueType;

/**
 * <p>
 * Represent an external function declaration, also known as <em>host
 * function</em> inside WebAssembly module.
 * </p>
 */
public interface ExternalFunctionDecl extends FunctionDecl {
	/**
	 * <p>
	 * Quick constant to be used as return value for void functions in
	 * {@link #onExec(Instance, Value[])}.
	 * </p>
	 */
	static Value[] VOID = new Value[0];

	/**
	 * <p>
	 * Execute this external function with parameters. The results will be pushed to
	 * operand stack, allowing the WebAssembly module to consume the results.
	 * </p>
	 * 
	 * @param instance The instance, usually {@link ExternalInstance} or
	 *                 {@code null}. You can use this to determine the import
	 *                 origin, or don't use it at all. The value is {@code null} if
	 *                 the external function is imported without instance.
	 * @param params   The parameters collected from operand stack. The number of
	 *                 values is equals to the size of inputs declared in
	 *                 {@link #type()}.
	 * @return The results to push to operand stack. The length must equals to the
	 *         size of outputs declared in {@link #type()} and each element must
	 *         have type equals to the respective type declared in function type.
	 */
	Value[] onExec(Instance instance, Value[] params);

	static ExternalFunctionDecl ofVoid(Runnable runnable) {
		return new ExternalFunctionDecl() {
			FunctionType type = new FunctionType(new ResultType(List.of()), new ResultType(List.of()));

			@Override
			public FunctionType type() {
				return type;
			}

			@Override
			public Value[] onExec(Instance instance, Value[] params) {
				runnable.run();
				return VOID;
			}
		};
	}

	static <P1> ExternalFunctionDecl ofVoid(ValueType p1, Consumer<? extends P1> consumer) {
		return new ExternalFunctionDecl() {
			FunctionType type = new FunctionType(new ValueType[] { p1 }, new ValueType[0]);

			@Override
			public FunctionType type() {
				return type;
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Value[] onExec(Instance instance, Value[] params) {
				((Consumer) consumer).accept(p1.mapToJava(params[0]));
				return VOID;
			}
		};
	}

	static <P1, P2> ExternalFunctionDecl ofVoid(ValueType p1, ValueType p2, BiConsumer<? extends P1, ? extends P2> consumer) {
		return new ExternalFunctionDecl() {
			FunctionType type = new FunctionType(new ValueType[] { p1, p2 }, new ValueType[0]);

			@Override
			public FunctionType type() {
				return type;
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Value[] onExec(Instance instance, Value[] params) {
				((BiConsumer) consumer).accept(p1.mapToJava(params[0]), p2.mapToJava(params[1]));
				return VOID;
			}
		};
	}

	static <R> ExternalFunctionDecl of(ValueType ret, Supplier<? extends R> supplier) {
		return new ExternalFunctionDecl() {
			FunctionType type = new FunctionType(new ValueType[] {}, new ValueType[] { ret });

			@Override
			public FunctionType type() {
				return type;
			}

			@Override
			public Value[] onExec(Instance instance, Value[] params) {
				return new Value[] { ret.mapFromJava(supplier.get()) };
			}
		};
	}

	static <R, P1> ExternalFunctionDecl of(ValueType ret, ValueType p1, Function<? extends P1, ? extends R> f) {
		return new ExternalFunctionDecl() {
			FunctionType type = new FunctionType(new ValueType[] { p1 }, new ValueType[] { ret });

			@Override
			public FunctionType type() {
				return type;
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public Value[] onExec(Instance instance, Value[] params) {
				return new Value[] { ret.mapFromJava(((Function) f).apply(p1.mapToJava(params[0]))) };
			}
		};
	}

	static <R, P1, P2> ExternalFunctionDecl of(ValueType ret, ValueType p1, ValueType p2, BiFunction<? extends P1, ? extends P2, ? extends R> f) {
		return new ExternalFunctionDecl() {
			FunctionType type = new FunctionType(new ValueType[] { p1, p2 }, new ValueType[] { ret });

			@Override
			public FunctionType type() {
				return type;
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public Value[] onExec(Instance instance, Value[] params) {
				return new Value[] {
					ret.mapFromJava(((BiFunction) f).apply(
						p1.mapToJava(params[0]),
						p2.mapToJava(params[1]))) };
			}
		};
	}
}
