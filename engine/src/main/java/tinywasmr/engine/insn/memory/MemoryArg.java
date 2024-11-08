package tinywasmr.engine.insn.memory;

/**
 * <p>
 * Define the offset and alignment. Currently only {@link #offset()} is used by
 * TinyWasmR; {@link #align()} will be ignored for the time being.
 * </p>
 */
public record MemoryArg(int offset, int align) {
}
