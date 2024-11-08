package tinywasmr.extern;

import tinywasmr.engine.type.value.NumberType;
import tinywasmr.engine.type.value.RefType;
import tinywasmr.engine.type.value.ValueType;

public class ReflectionUtils {
	public static ValueType classToValueType(Class<?> clazz) {
		if (clazz == void.class || clazz == Void.class) return null;
		if (clazz == int.class || clazz == Integer.class) return NumberType.I32;
		if (clazz == long.class || clazz == Long.class) return NumberType.I64;
		if (clazz == float.class || clazz == Float.class) return NumberType.F32;
		if (clazz == double.class || clazz == Double.class) return NumberType.F64;
		return RefType.EXTERN;
	}
}
