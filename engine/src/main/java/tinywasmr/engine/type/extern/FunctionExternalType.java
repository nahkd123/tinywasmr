package tinywasmr.engine.type.extern;

import tinywasmr.engine.type.FunctionType;

public record FunctionExternalType(FunctionType funcType) implements ExternalType {
}
