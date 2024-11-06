package tinywasmr.parser.binary;

import java.util.List;

import tinywasmr.engine.type.value.ValueType;

record BinaryFunctionBody(List<ValueType> locals, List<InstructionBuilder> body) {
}
