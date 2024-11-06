package tinywasmr.engine.type.extern;

import tinywasmr.engine.type.TableType;

public record TableExternalType(TableType tableType) implements ExternalType {
}
