package tinywasmr.w4;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Consumer;

import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.extern.annotation.Export;

/**
 * <p>
 * Represent WASM-4 environment that the game can interact with. Usually used as
 * environment module that will be imported to the game cartridge.
 * </p>
 */
// https://github.com/aduros/wasm4/blob/main/runtimes/native/src/framebuffer.c
public class W4Environment {
	// Addresses
	public static final int GAMEPAD_ADDRESS = 0x0016;
	public static final int MOUSE_ADDRESS = 0x001A;

	// Environment access
	private W4DiskAccess disk;
	private Consumer<String> trace;
	private W4Framebuffer framebuffer;

	@Export(exportAs = "memory")
	private byte[] memory = new byte[Memory.PAGE_SIZE];

	public W4Environment(W4DiskAccess disk, Consumer<String> trace) {
		this.disk = disk;
		this.trace = trace != null ? trace : s -> {};
		this.framebuffer = new W4Framebuffer(memory);
	}

	/**
	 * <p>
	 * Get the main memory. The memory is a linear memory with 64KiB in size, fit
	 * enough in one page of WebAssembly memory.
	 * </p>
	 */
	public byte[] getMemory() { return memory; }

	/**
	 * <p>
	 * Get the framebuffer with useful methods for drawing and rendering.
	 * </p>
	 */
	public W4Framebuffer getFramebuffer() { return framebuffer; }

	public String getNulTermAscii(int address) {
		int len = 0;
		while (memory[address + len] != 0x00) len++;
		byte[] bs = new byte[len];
		System.arraycopy(memory, address, bs, 0, len);
		return new String(bs, StandardCharsets.US_ASCII);
	}

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
		blitSub(address, x, y, width, height, 0, 0, width, flags);
	}

	@Export(exportAs = "blitSub")
	public void blitSub(int address, int x, int y, int width, int height, int srcX, int srcY, int stride, int flags) {
		framebuffer.blit(memory, address, x, y, width, height, srcX, srcY, stride, flags);
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
		framebuffer.rect(x, y, width, height);
	}

	@Export(exportAs = "text")
	public void text(int address, int x, int y) {
		int xx = x;

		for (int p = address; p < memory.length; p++) {
			int ch = memory[p] & 0xff;
			if (ch == 0) return;

			if (ch == 10) {
				y += 8;
				xx = x;
				continue;
			}

			if (ch >= 32 && ch <= 255)
				framebuffer.blit(framebuffer.getFontRom(), 0, xx, y, 8, 8, 0, (ch - 32) << 3, 8, 0);
			xx += 8;
		}
	}

	@Export(exportAs = "tone")
	public void tone(int freq, int duration, int volume, int flags) {
		// TODO implement tone
		trace.accept("env::tone(): not implemented");
	}

	@Export(exportAs = "trace")
	public void trace(int address) {
		trace.accept(getNulTermAscii(address));
	}
}
