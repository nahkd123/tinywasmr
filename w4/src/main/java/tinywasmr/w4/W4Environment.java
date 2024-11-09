package tinywasmr.w4;

import java.util.Arrays;
import java.util.function.Consumer;

import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.extern.annotation.Export;

/**
 * <p>
 * Represent WASM-4 environment that the game can interact with.
 * </p>
 */
public class W4Environment {
	// Constants
	public static final int SCREEN_SIZE = 160;
	public static final int FONT_SIZE = 8;

	// Addresses
	public static final int PALETTE_ADDRESS = 0x0004;
	public static final int DRAW_COLORS_ADDRESS = 0x0014;
	public static final int GAMEPAD_ADDRESS = 0x0016;
	public static final int MOUSE_ADDRESS = 0x001A;
	public static final int FRAMEBUFFER_ADDRESS = 0x00A0;

	// Environment access
	private W4DiskAccess disk;
	private Consumer<String> trace;

	@Export(exportAs = "memory")
	private byte[] memory = new byte[Memory.PAGE_SIZE];

	public W4Environment(W4DiskAccess disk, Consumer<String> trace) {
		this.disk = disk;
		this.trace = trace != null ? trace : s -> {};
	}

	/**
	 * <p>
	 * Get the main memory. The memory is a linear memory with 64KiB in size, fit
	 * enough in one page of WebAssembly memory.
	 * </p>
	 */
	public byte[] getMemory() { return memory; }

	@Export(exportAs = "diskr")
	public int diskr(int address, int size) {
		if (disk != null) {
			disk.read(memory, address, size);
		} else {
			trace.accept("env::diskr() failed: no disk access provided");
			Arrays.fill(memory, address, address + size, (byte) 0);
		}

		return 0;
	}

	@Export(exportAs = "diskw")
	public int diskw(int address, int size) {
		if (disk != null) {
			disk.write(memory, address, size);
		} else {
			trace.accept("env::diskw() failed: no disk access provided");
		}

		return 0;
	}

	@Export(exportAs = "blit")
	public void blit(int address, int x, int y, int width, int height, int flags) {
		// TODO implement blit
		trace.accept("env::blit(): not implemented");
	}

	@Export(exportAs = "blitSub")
	public void blitSub(int address, int x, int y, int width, int height, int srcX, int srcY, int stride, int flags) {
		// TODO implement blitSub
		trace.accept("env::blitSub(): not implemented");
	}

	@Export(exportAs = "line")
	public void line(int x, int y, int width, int height) {
		// TODO implement line
		trace.accept("env::line(): not implemented");
	}

	@Export(exportAs = "oval")
	public void oval(int x, int y, int width, int height) {
		// TODO implement oval
		trace.accept("env::oval(): not implemented");
	}

	@Export(exportAs = "rect")
	public void rect(int x, int y, int width, int height) {
		// TODO implement rect
		trace.accept("env::rect(): not implemented");
	}

	@Export(exportAs = "text")
	public void text(int address, int x, int y) {
		// TODO implement text
		trace.accept("env::text(): not implemented");
	}

	@Export(exportAs = "tone")
	public void tone(int freq, int duration, int volume, int flags) {
		// TODO implement tone
		trace.accept("env::tone(): not implemented");
	}

	@Export(exportAs = "trace")
	public void trace(int address) {
		StringBuilder builder = new StringBuilder();
		while (address < memory.length && memory[address] != 0x00) builder.append((char) (memory[address++] & 0xFF));
		trace.accept(builder.toString());
	}
}
