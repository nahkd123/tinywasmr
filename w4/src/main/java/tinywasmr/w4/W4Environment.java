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

	private void setPixel(int x, int y, int col) {
		int shift = 6 - ((x % 4) << 1);
		int addr = FRAMEBUFFER_ADDRESS + y * SCREEN_SIZE / 4 + x / 4;
		memory[addr] &= ~(3 << shift);
		memory[addr] |= col << shift;
	}

	private int texRead1bbp(int address, int x, int y, int w, int h) {
		if (w <= 4) {
			int rpb = 8 / w; // rows per byte
			// TODO
		} else {
			int bpr = 1 + ((w - 1) / 8); // bytes per row
			int addr = address + y * bpr + x / 8;
			int mask = 1 << (x % 8);
			if ((memory[addr] & mask) != 0) return 1;
		}

		return 0;
	}

	private int texRead2bbp(int address, int x, int y, int w, int h) {
		// TODO
		return 0;
	}

	@Export(exportAs = "blit")
	public void blit(int address, int x, int y, int width, int height, int flags) {
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int pixCol = (flags & BLIT_2BPP) != 0
					? texRead2bbp(address, x + col, y + row, width, height)
					: texRead1bbp(address, col, row, width, height);
				setPixel(x + col, y + row, pixCol);
				// TODO flip and rotate
			}
		}
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
		int drawCol = getDrawColor();
		int fill = (drawCol & 0x000f);
		int outline = (drawCol & 0x00f0) >> 4;

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				boolean isOutline = col == 0 || col == width - 1 || row == 0 || row == height - 1;
				int pixCol = isOutline && outline != 0 ? outline : fill;
				if (pixCol == 0) continue;
				setPixel(x + col, y + row, pixCol - 1);
			}
		}
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
