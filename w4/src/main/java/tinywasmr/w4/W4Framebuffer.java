package tinywasmr.w4;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;

/**
 * <p>
 * The framebuffer view, ported straight from <a href=
 * "https://github.com/aduros/wasm4/blob/main/runtimes/native/src/framebuffer.c">WASM-4
 * framebuffer.c</a>
 */
public class W4Framebuffer {
	/**
	 * <p>
	 * The size of the screen in both axes: width and height. Each pixel consumes 2
	 * bits of the framebuffer, which points towards 4 colors defined in palette.
	 * </p>
	 */
	public static final int SCREEN_SIZE = 160;
	public static final int FONT_SIZE = 8;
	private static final int WIDTH = SCREEN_SIZE;
	private static final int HEIGHT = SCREEN_SIZE;

	public static final int PALETTE = 0x0004;
	public static final int DRAW_COLORS = 0x0014;
	public static final int BASE = 0x00A0;

	public static final int BLIT_2BPP = 0b0001;
	public static final int BLIT_FLIP_X = 0b0010;
	public static final int BLIT_FLIP_Y = 0b0100;
	public static final int BLIT_ROTATE = 0b1000;

	private byte[] memory;
	private byte[] fontRom;

	public W4Framebuffer(byte[] memory, byte[] fontRom) {
		this.memory = memory;
		this.fontRom = fontRom;
	}

	public W4Framebuffer(byte[] memory) {
		this.memory = memory;

		try (InputStream stream = getClass().getClassLoader().getResourceAsStream("w4font.bin")) {
			if (stream == null) throw new RuntimeException("w4font.bin resource is missing");
			this.fontRom = stream.readNBytes(1792);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public byte[] getMemory() { return memory; }

	public byte[] getFontRom() { return fontRom; }

	public void clear() {
		Arrays.fill(memory, BASE, BASE + ((WIDTH * HEIGHT) >> 2), (byte) 0x00);
	}

	public int getPaletteRGB(int index) {
		int addr = PALETTE + index * 4;
		int val = (memory[addr + 0] & 0xff)
			| (memory[addr + 1] & 0xff) << 8
			| (memory[addr + 2] & 0xff) << 16
			| (memory[addr + 3] & 0xff) << 24;
		return val;
	}

	/**
	 * <p>
	 * Get the color palette in BGR format, with color data stored in least
	 * significant 24 bits. The most significant 8 bits are always 0.
	 * </p>
	 * <p>
	 * This method was meant to be used for setting OpenGL texture pixels.
	 * </p>
	 * 
	 * @param index Palette index from 0 to 3.
	 * @return The color in {@code 0x00BBGGRR} format.
	 */
	public int getPaletteBGR(int index) {
		int addr = PALETTE + index * 4;
		int val = (memory[addr + 2] & 0xff)
			| (memory[addr + 1] & 0xff) << 8
			| (memory[addr + 0] & 0xff) << 16;
		return val;
	}

	public int getDrawColor() { return (memory[DRAW_COLORS] & 0xff) | (memory[DRAW_COLORS] & 0xff) << 8; }

	public void drawPoint(int color, int x, int y) {
		int idx = (WIDTH * y + x) >> 2;
		int shift = (x & 0x3) << 1;
		int mask = 0x3 << shift;
		memory[BASE + idx] = (byte) ((color << shift) | (memory[BASE + idx] & ~mask));
	}

	public void drawHLine(int color, int startX, int y, int endX) {
		int fillEnd = endX - (endX & 3);
		int fillStart = Math.min((startX + 3) & ~3, fillEnd);

		if (fillEnd - fillStart > 3) {
			for (int xx = startX; xx < fillStart; xx++) drawPoint(color, xx, y);
			int from = (WIDTH * y + fillStart) >> 2;
			int to = (WIDTH * y + fillEnd) >> 2;
			int fillColor = color * 0x55;
			Arrays.fill(memory, BASE + from, BASE + to, (byte) fillColor);
			startX = fillEnd;
		}

		for (int xx = startX; xx < endX; xx++) drawPoint(color, xx, y);
	}

	public void rect(int x, int y, int width, int height) {
		int startX = Math.max(0, x);
		int startY = Math.max(0, y);
		int endXUnclamped = x + width;
		int endYUnclamped = y + height;
		int endX = Math.max(0, Math.min(endXUnclamped, WIDTH));
		int endY = Math.max(0, Math.min(endYUnclamped, HEIGHT));

		int dc01 = getDrawColor();
		int dc0 = dc01 & 0xf;
		int dc1 = (dc01 >> 4) & 0xf;

		if (dc0 != 0) {
			int fillColor = (dc0 - 1) & 0x3;
			for (int yy = startY; yy < endY; ++yy) drawHLine(fillColor, startX, yy, endX);
		}

		if (dc1 != 0) {
			int strokeColor = (dc1 - 1) & 0x3;
			if (x >= 0 && x < WIDTH) for (int yy = startY; yy < endY; ++yy) drawPoint(strokeColor, x, yy);
			if (endXUnclamped > 0 && endXUnclamped <= WIDTH)
				for (int yy = startY; yy < endY; ++yy) drawPoint(strokeColor, endXUnclamped - 1, yy);
			if (y >= 0 && y < HEIGHT) drawHLine(strokeColor, startX, y, endX);
			if (endYUnclamped > 0 && endYUnclamped <= HEIGHT) drawHLine(strokeColor, startX, endYUnclamped - 1, endX);
		}
	}

	public void blit(byte[] spritesheet, int spriteBase, int dstX, int dstY, int width, int height, int srcX, int srcY, int srcStride, int flags) {
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
			clipXMax = Math.min(width, HEIGHT - dstY);
			clipYMax = Math.min(height, WIDTH - dstX);
		} else {
			clipXMin = Math.max(0, dstX) - dstX;
			clipYMin = Math.max(0, dstY) - dstY;
			clipXMax = Math.min(width, WIDTH - dstX);
			clipYMax = Math.min(height, HEIGHT - dstY);
		}

		for (int y = clipYMin; y < clipYMax; y++) {
			for (int x = clipXMin; x < clipXMax; x++) {
				int tx = dstX + (rotate ? y : x);
				int ty = dstY + (rotate ? x : y);
				int sx = srcX + (flipX ? width - x - 1 : x);
				int sy = srcY + (flipY ? height - y - 1 : y);
				int colorIdx;
				int bitIndex = sy * srcStride + sx;

				if (bpp2) {
					int v = spritesheet[spriteBase + (bitIndex >> 2)] & 0xff;
					int shift = 6 - ((bitIndex & 0x03) << 1);
					colorIdx = (v >> shift) & 0x3;

				} else {
					int v = spritesheet[spriteBase + (bitIndex >> 3)] & 0xff;
					int shift = 7 - (bitIndex & 0x07);
					colorIdx = (v >> shift) & 0x1;
				}

				int dc = (colors >> (colorIdx << 2)) & 0x0f;
				if (dc != 0) drawPoint((dc - 1) & 0x03, tx, ty);
			}
		}
	}
}
