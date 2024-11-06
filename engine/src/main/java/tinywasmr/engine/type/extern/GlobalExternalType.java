package tinywasmr.engine.type.extern;

import tinywasmr.engine.type.GlobalType;

public record GlobalExternalType(GlobalType globalType) implements ExternalType {
}
