package tinywasmr.engine.type;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import tinywasmr.engine.type.value.ValueType;

/**
 * <p>
 * Result types classify the result of executing instructions or functions,
 * which is a sequence of values.
 * </p>
 *
 * @see <a href=
 *      "https://webassembly.github.io/spec/core/syntax/types.html#result-types">WebAssembly
 *      Core Specification - Types - Result Types</a>
 */
public record ResultType(List<ValueType> types) implements BlockType {
	public ResultType {
		types = Collections.unmodifiableList(types);
	}

	@Override
	public List<ValueType> blockResults() {
		return types;
	}

	@Override
	public final String toString() {
		if (types.size() == 0) return "void";
		if (types.size() == 1) return types.get(0).toString();
		return "(%s)".formatted(types.stream().map(ValueType::toString).collect(Collectors.joining(", ")));
	}
}
