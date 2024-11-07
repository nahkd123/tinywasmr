package tinywasmr.engine.type;

import java.util.List;

import tinywasmr.engine.type.value.ValueType;

public record FunctionType(ResultType inputs, ResultType outputs) {
	public FunctionType(ValueType[] inputs, ValueType[] outputs) {
		this(new ResultType(List.of(inputs)), new ResultType(List.of(outputs)));
	}
}
