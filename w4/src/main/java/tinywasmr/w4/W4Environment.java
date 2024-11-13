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
	public static final int SYSTEM_FLAGS_ADDR = 0x001F;
	public static final int SYSTEM_PRESERVE_FB = 0b00000001;
	public static final int SYSTEM_HIDE_GAMEPAD_OVERLAY = 0b00000010;

	// Environment access
	private W4DiskAccess disk;
	private Consumer<String> trace;
	private W4Framebuffer framebuffer;
	private W4Input input;

	@Export(exportAs = "memory")
	private byte[] memory = new byte[Memory.PAGE_SIZE];

	public W4Environment(W4DiskAccess disk, Consumer<String> trace) {
		this.disk = disk;
		this.trace = trace != null ? trace : s -> {};
		this.framebuffer = new W4Framebuffer(memory);
		this.input = new W4Input(memory);
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

	/**
	 * <p>
	 * Get the input manager for controlling the game.
	 * </p>
	 */
	public W4Input getInput() { return input; }

	public int getSystemFlags() { return memory[SYSTEM_FLAGS_ADDR] & 0xff; }

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
	public void line(int x1, int y1, int x2, int y2) {
		framebuffer.line(x1, y1, x2, y2);
	}

	@Export(exportAs = "oval")
	public void oval(int x, int y, int width, int height) {
		framebuffer.oval(x, y, width, height);
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
	public void tone(int frequency, int duration, int volume, int flags) {
		// int freq1 = frequency & 0xffff;
		// int freq2 = (frequency >> 16) & 0xffff;
		// int sustain = (duration & 0xff);
		// int release = ((duration >> 8) & 0xff);
		// int decay = ((duration >> 16) & 0xff);
		// int attack = ((duration >> 24) & 0xff);
		// int sustainVolume = Math.min(volume & 0xff, 100);
		// int peakVolume = Math.min((volume >> 8) & 0xff, 100);
		// int channelIdx = flags & 0x3;
		// int mode = (flags >> 2) & 0x3;
		// int pan = (flags >> 4) & 0x3;
		// int noteMode = flags & 0x40;
		// TODO implement tone
	}

	@Export(exportAs = "trace")
	public void trace(int address) {
		trace.accept(getNulTermAscii(address));
	}

	@Export(exportAs = "hline")
	public void hline(int x, int y, int len) {
		int dc0 = framebuffer.getDrawColor() & 0xf;
		framebuffer.drawHLine(dc0, x, y, x + len);
	}

	@Export(exportAs = "vline")
	public void vline(int x, int y, int len) {
		framebuffer.drawVLine(x, y, len);
	}
}
