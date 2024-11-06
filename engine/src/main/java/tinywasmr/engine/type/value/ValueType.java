package tinywasmr.engine.type.value;

import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.type.BlockType;

/**
 * <p>
 * Value types classify the individual values that WebAssembly code can compute
 * with and the values that a variable accepts. They are either number types,
 * vector types, or reference types.
 * </p>
 *
 * @see <a href=
 *      "https://webassembly.github.io/spec/core/syntax/types.html#value-types">WebAssembly
 *      Core Specification - Types - Value Types</a>
 */
public sealed interface ValueType extends BlockType permits NumberType, RefType, VectorType {
	Value zero();
}
