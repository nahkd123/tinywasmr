package tinywasmr.parser.binary;

import java.util.List;

import tinywasmr.engine.type.value.ValueType;

public record BinaryFunctionBody(List<ValueType> locals, List<BinaryInstructionBuilder> body) {
}
