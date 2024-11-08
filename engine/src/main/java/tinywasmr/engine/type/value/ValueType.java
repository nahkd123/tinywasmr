package tinywasmr.engine.type.value;

import tinywasmr.engine.exec.ValidationException;
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

	/**
	 * <p>
	 * Attempt to map Java object to {@link Value}, which then can be consumed by
	 * WebAssembly code. If mapping failed, it will throws
	 * {@link IllegalArgumentException}.
	 * </p>
	 * 
	 * @param object The object from Java.
	 * @return The value that can be passed to WebAssembly.
	 * @throws IllegalArgumentException if mapping failed because the type is
	 *                                  incompatible.
	 */
	Value mapFromJava(Object object);

	/**
	 * <p>
	 * Attempt to map from {@link Value} to Java object, which then can be consumed
	 * by Java applications. If mapping failed, it will throws
	 * {@link IllegalArgumentException}.
	 * </p>
	 * 
	 * @param value The value from WebAssembly.
	 * @return The value that can be consumed by Java applications.
	 * @throws IllegalArgumentException if mapping failed because the type is
	 *                                  incompatible.
	 * @throws ValidationException      if the value type does not match with this
	 *                                  type.
	 */
	Object mapToJava(Value value);
}
