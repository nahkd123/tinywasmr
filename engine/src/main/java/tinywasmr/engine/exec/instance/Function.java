package tinywasmr.engine.exec.instance;

import java.util.List;

import tinywasmr.engine.exec.executor.DefaultExecutor;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.DefaultMachine;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.type.FunctionType;
import tinywasmr.engine.type.value.ValueType;

public record Function(Instance instance, FunctionDecl declaration) implements Exportable {
	public FunctionType type() {
		return declaration.type();
	}

	/**
	 * <p>
	 * Execute this function, using default executor and virtual machine
	 * implementation ({@link DefaultExecutor} and {@link DefaultMachine}), and
	 * return all the results as array of values.
	 * </p>
	 * <p>
	 * The mapping between WebAssembly type to Java type is as follows:
	 * <ul>
	 * <li>{@code i32} to {@code int}</li>
	 * <li>{@code i64} to {@code long}</li>
	 * <li>{@code f32} to {@code float}</li>
	 * <li>{@code f64} to {@code double}</li>
	 * <li>{@code v128} to {@code long[2]}</li>
	 * <li>{@code funcRef} to {@link Function}</li>
	 * <li>{@code externRef} to (not implemented)</li>
	 * </ul>
	 * </p>
	 * 
	 * @param params A list of parameters to execute.
	 * @return An array of values.
	 */
	public Object[] executeToArray(Object[] params) {
		List<ValueType> paramTypes = declaration.type().inputs().types();
		List<ValueType> resultTypes = declaration.type().outputs().types();

		if (params.length != paramTypes.size()) throw new IllegalArgumentException("Expecting %d params, found %d"
			.formatted(paramTypes.size(), params.length));

		Value[] inputs = new Value[params.length];
		for (int i = 0; i < inputs.length; i++) inputs[i] = paramTypes.get(i).mapFromJava(params[i]);

		Value[] outputs = new DefaultExecutor().execute(this, inputs);
		if (outputs.length != resultTypes.size()) throw new IllegalArgumentException("Expecting %d results, found %d"
			.formatted(resultTypes.size(), outputs.length));

		Object[] results = new Object[outputs.length];
		for (int i = 0; i < outputs.length; i++) results[i] = resultTypes.get(i).mapToJava(outputs[i]);
		return results;
	}

	/**
	 * <p>
	 * Varargs version of {@link #executeToArray(Object[])}.
	 * </p>
	 */
	public Object[] execToArray(Object... params) {
		return executeToArray(params);
	}

	/**
	 * <p>
	 * Similar to {@link #executeToArray(Object[])}, but the return value is
	 * {@code null} if the declared result is empty, {@code results[0]} if the
	 * result have 1 element and {@code results} if the result have more than 2
	 * elements.
	 * </p>
	 */
	public Object execute(Object[] params) {
		Object[] results = executeToArray(params);
		return results.length == 0 ? null : results.length == 1 ? results[0] : results;
	}

	/**
	 * <p>
	 * Varargs version of {@link #execute(Object[])}
	 * </p>
	 */
	public Object exec(Object... params) {
		return execute(params);
	}
}
