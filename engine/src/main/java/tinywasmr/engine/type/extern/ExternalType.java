package tinywasmr.engine.type.extern;

public sealed interface ExternalType permits FunctionExternalType, TableExternalType, MemoryExternalType, GlobalExternalType {
}
