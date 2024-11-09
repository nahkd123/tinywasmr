package tinywasmr.w4;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Consumer;

import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.extern.annotation.Export;

/**
 * <p>
 * Represent WASM-4 environment that the game can interact with.
 * </p>
 */
// https://github.com/aduros/wasm4/blob/main/runtimes/native/src/framebuffer.c
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

	// Flags
	public static final int BLIT_2BPP = 0b0001;
	public static final int BLIT_FLIP_X = 0b0010;
	public static final int BLIT_FLIP_Y = 0b0100;
	public static final int BLIT_ROTATE = 0b1000;

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

	public int getPaletteRGB(int index) {
		int addr = PALETTE_ADDRESS + index * 4;
		int val = (memory[addr + 0] & 0xff)
			| (memory[addr + 1] & 0xff) << 8
			| (memory[addr + 2] & 0xff) << 16
			| (memory[addr + 3] & 0xff) << 24;
		return val;
	}

	public int getPaletteBGR(int index) {
		int addr = PALETTE_ADDRESS + index * 4;
		int val = (memory[addr + 2] & 0xff)
			| (memory[addr + 1] & 0xff) << 8
			| (memory[addr + 0] & 0xff) << 16;
		return val;
	}

	public int getDrawColor() {
		return (memory[DRAW_COLORS_ADDRESS] & 0xff) | (memory[DRAW_COLORS_ADDRESS] & 0xff) << 8;
	}

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

	public void setPixel(int x, int y, int col) {
		int addr = FRAMEBUFFER_ADDRESS + ((SCREEN_SIZE * y + x) >> 2);
		int shift = (x & 3) << 1;
		int mask = 0b11 << shift;
		memory[addr] = (byte) ((col << shift) | ((memory[addr] & 0xFF) & ~mask));
	}

	@Export(exportAs = "blit")
	public void blit(int address, int x, int y, int width, int height, int flags) {
		blitSub(address, x, y, width, height, 0, 0, width, flags);
	}

	@Export(exportAs = "blitSub")
	public void blitSub(int address, int dstX, int dstY, int width, int height, int srcX, int srcY, int stride, int flags) {
		boolean bpp2 = (flags & BLIT_2BPP) != 0;
		boolean flipX = (flags & BLIT_FLIP_X) != 0;
		boolean flipY = (flags & BLIT_FLIP_Y) != 0;
		boolean rotate = (flags & BLIT_ROTATE) != 0;
		int colors = getDrawColor();
		int clipXMin, clipYMin, clipXMax, clipYMax;

		if (rotate) {
			flipX = !flipX;
			clipXMin = Math.max(0, dstY) - dstY;
			clipYMin = Math.max(0, dstX) - dstX;
			clipXMax = Math.min(width, SCREEN_SIZE - dstY);
			clipYMax = Math.min(height, SCREEN_SIZE - dstX);
		} else {
			clipXMin = Math.max(0, dstX) - dstX;
			clipYMin = Math.max(0, dstY) - dstY;
			clipXMax = Math.min(width, SCREEN_SIZE - dstX);
			clipYMax = Math.min(height, SCREEN_SIZE - dstY);
		}

		for (int y = clipYMin; y < clipYMax; y++) {
			for (int x = clipXMin; x < clipXMax; x++) {
				int tx = dstX + (rotate ? y : x);
				int ty = dstY + (rotate ? x : y);
				int sx = srcX + (flipX ? width - x - 1 : x);
				int sy = srcY + (flipY ? height - y - 1 : y);
				int colorIdx;
				int bitIndex = sy * stride + sx;

				if (bpp2) {
					int b = memory[address + (bitIndex >> 2)] & 0xff;
					int shift = 6 - ((bitIndex & 0x03) << 1);
					colorIdx = (b >> shift) & 0x3;
				} else {
					int b = memory[address + (bitIndex >> 3)] & 0xff;
					int shift = 7 - (bitIndex & 0x07);
					colorIdx = (b >> shift) & 0x1;
				}

				int dc = (colors >> (colorIdx << 2)) & 0x0f;
				if (dc != 0) setPixel(tx, ty, (dc - 1) & 0x03);
			}
		}
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
		int dc01 = getDrawColor();
		int dc0 = (dc01 & 0x000f);
		int dc1 = (dc01 & 0x00f0) >> 4;

		int startX = Math.max(0, x);
		int startY = Math.max(0, y);
		int endXUnclamped = x + width;
		int endYUnclamped = y + height;
		int endX = Math.max(0, Math.min(endXUnclamped, SCREEN_SIZE));
		int endY = Math.max(0, Math.min(endYUnclamped, SCREEN_SIZE));

		if (dc0 != 0) {
			int fillColor = (dc0 - 1) & 0x3;
			for (int yy = startY; yy < endY; ++yy) drawHLine(fillColor, startX, yy, endX);
		}

		if (dc1 != 0) {
			int strokeColor = (dc1 - 1) & 0x3;
			if (x >= 0 && x < SCREEN_SIZE) for (int yy = startY; yy < endY; ++yy) setPixel(x, yy, strokeColor);
			if (endXUnclamped > 0 && endXUnclamped <= SCREEN_SIZE)
				for (int yy = startY; yy < endY; ++yy) setPixel(endXUnclamped - 1, yy, strokeColor);
			if (y >= 0 && y < SCREEN_SIZE) drawHLine(startX, y, endX, strokeColor);
			if (endYUnclamped > 0 && endYUnclamped <= SCREEN_SIZE)
				drawHLine(strokeColor, startX, endYUnclamped - 1, endX);
		}
	}

	private void drawHLine(int color, int startX, int y, int endX) {
		int fillEnd = endX - (endX & 3);
		int fillStart = Math.min((startX + 3) & ~3, fillEnd);

		if (fillEnd - fillStart > 3) {
			for (int xx = startX; xx < fillStart; xx++) setPixel(xx, y, color);

			int from = (SCREEN_SIZE * y + fillStart) >> 2;
			int to = (SCREEN_SIZE * y + fillEnd) >> 2;
			int fillColor = color * 0x55;

			Arrays.fill(memory, FRAMEBUFFER_ADDRESS + from, FRAMEBUFFER_ADDRESS + to, (byte) fillColor);
			startX = fillEnd;
		}

		for (int xx = startX; xx < endX; xx++) setPixel(xx, y, color);
	}

	@Export(exportAs = "text")
	public void text(int address, int x, int y) {
		// TODO implement text
		// trace.accept("env::text(): not implemented");
		trace.accept("env::text(\"%s\", %d, %d)".formatted(getNulTermAscii(address), x, y));

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
