package tinywasmr.engine.type;

import tinywasmr.engine.type.value.ValueType;

public record GlobalType(Mutablity mutablity, ValueType valType) {
}
