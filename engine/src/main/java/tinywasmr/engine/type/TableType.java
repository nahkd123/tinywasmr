package tinywasmr.engine.type;

import tinywasmr.engine.type.value.RefType;

public record TableType(Limit limit, RefType refType) {
}
