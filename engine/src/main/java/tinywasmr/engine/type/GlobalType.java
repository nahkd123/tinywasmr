package tinywasmr.engine.type;

import tinywasmr.engine.type.value.ValueType;

public record GlobalType(Mutability mutablity, ValueType valType) {
}
