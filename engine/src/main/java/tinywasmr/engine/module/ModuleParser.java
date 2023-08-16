package tinywasmr.engine.module;

import java.io.IOException;

import tinywasmr.engine.io.LEDataInput;

public interface ModuleParser {
	public int getFormatVersion();

	/**
	 * <p>
	 * Attempt to parse WebAssembly module from input.
	 * </p>
	 * <p>
	 * This method assumes the header (the one with
	 * {@code "\x00asm" + (u32 version)}) is not present in the stream.
	 * </p>
	 * 
	 * @param in Input stream.
	 * @return The parsed module. Never return {@code null}. If the parsing process
	 *         failed, this method will throws {@link IOException}.
	 * @throws IOException
	 */
	public WasmModule parse(LEDataInput in) throws IOException;
}
