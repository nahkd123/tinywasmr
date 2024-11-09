package tinywasmr.engine.module.func.extern;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import tinywasmr.engine.exec.frame.FunctionFrame;
import tinywasmr.engine.exec.instance.Instance;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.type.FunctionType;
import tinywasmr.engine.type.ResultType;
import tinywasmr.engine.type.value.ValueType;

/**
 * <p>
 * Unlike {@link ExternalFunctionDecl}, where the host function may execute
 * guest function as callback mechanism, this interface is designed for host
 * function that will never call guest function.
 * </p>
 */
public interface HostOnlyFunctionDecl extends ExternalFunctionDecl {
	static Value[] VOID = new Value[0];

	public Value[] onExec(Instance instance, Value[] parameter);

	@Override
	default void onStep(Machine vm, FunctionFrame frame, Value[] locals, int stepIndex) {
		vm.exitFunction(onExec(frame.getInstance(), locals));
	}

	static HostOnlyFunctionDecl ofVoid(Runnable runnable) {
		return new HostOnlyFunctionDecl() {
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

	static <P1> HostOnlyFunctionDecl ofVoid(ValueType p1, Consumer<? extends P1> consumer) {
		return new HostOnlyFunctionDecl() {
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

	static <P1, P2> HostOnlyFunctionDecl ofVoid(ValueType p1, ValueType p2, BiConsumer<? extends P1, ? extends P2> consumer) {
		return new HostOnlyFunctionDecl() {
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

	static <R> HostOnlyFunctionDecl of(ValueType ret, Supplier<? extends R> supplier) {
		return new HostOnlyFunctionDecl() {
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

	static <R, P1> HostOnlyFunctionDecl of(ValueType ret, ValueType p1, Function<? extends P1, ? extends R> f) {
		return new HostOnlyFunctionDecl() {
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

	static <R, P1, P2> HostOnlyFunctionDecl of(ValueType ret, ValueType p1, ValueType p2, BiFunction<? extends P1, ? extends P2, ? extends R> f) {
		return new HostOnlyFunctionDecl() {
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
