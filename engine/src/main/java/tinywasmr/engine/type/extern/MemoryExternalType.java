package tinywasmr.engine.type.extern;

import tinywasmr.engine.type.MemoryType;

public record MemoryExternalType(MemoryType memType) implements ExternalType {
}
