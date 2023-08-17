package tinywasmr.engine.module.type;

public enum TypeEnum {
	I32,
	I64,
	F32,
	F64,
	V128,
	// TODO reference types
	// TODO value types
	// TODO result types
	FUNCTION,

	// Only available for blocks
	VOID,
}
