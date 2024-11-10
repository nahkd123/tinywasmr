package tinywasmr.w4.gui;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ROW_LENGTH;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static tinywasmr.w4.W4Framebuffer.SCREEN_SIZE;

import org.lwjgl.opengl.GL30;

import imgui.internal.ImGui;
import tinywasmr.w4.W4Framebuffer;

public class W4Screen implements AutoCloseable {
	public static final int PX_PER_BYTE = 4;
	public static final int BYTE_PER_ROW = SCREEN_SIZE / PX_PER_BYTE;

	private int[] pixels;
	private int texId;

	public W4Screen() {
		pixels = new int[SCREEN_SIZE * SCREEN_SIZE];
		texId = GL30.glGenTextures();
		GL30.glBindTexture(GL_TEXTURE_2D, texId);
		GL30.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST); // Nearest neighbour
		GL30.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		GL30.glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
		// Test pattern
		pixels[0] = 0xFFFF0000; // Blue
		pixels[1] = 0xFFFF0000;
		pixels[2] = 0xFF00FF00; // Green
		pixels[3] = 0xFF00FF00;
		pixels[4] = 0xFF0000FF; // Red
		pixels[5] = 0xFF0000FF;
		GL30.glTexImage2D(GL_TEXTURE_2D,
			0, GL_RGBA, SCREEN_SIZE, SCREEN_SIZE,
			0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
	}

	public void uploadTexture(W4Framebuffer fb) {
		byte[] ram = fb.getMemory();
		int[] palette = new int[] {
			// ABGR
			0xFF000000 | fb.getPaletteBGR(0),
			0xFF000000 | fb.getPaletteBGR(1),
			0xFF000000 | fb.getPaletteBGR(2),
			0xFF000000 | fb.getPaletteBGR(3),
		};

		for (int row = 0; row < SCREEN_SIZE; row++) {
			int rowStart = row * SCREEN_SIZE;
			for (int col = 0; col < SCREEN_SIZE; col += PX_PER_BYTE) {
				int b4addr = W4Framebuffer.BASE + row * BYTE_PER_ROW + col / PX_PER_BYTE;
				int colIdx = rowStart + col;
				byte b4 = ram[b4addr];
				pixels[colIdx + 0] = palette[b4 & 0b00000011];
				pixels[colIdx + 1] = palette[(b4 & 0b00001100) >> 2];
				pixels[colIdx + 2] = palette[(b4 & 0b00110000) >> 4];
				pixels[colIdx + 3] = palette[(b4 & 0b11000000) >> 6];
			}
		}

		GL30.glBindTexture(GL_TEXTURE_2D, texId);
		GL30.glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
		GL30.glTexImage2D(GL_TEXTURE_2D,
			0, GL_RGBA, SCREEN_SIZE, SCREEN_SIZE,
			0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
	}

	public void screen(W4Framebuffer fb, float screenSize) {
		if (fb != null) uploadTexture(fb);
		ImGui.image(texId, screenSize, screenSize);
	}

	@Override
	public void close() {
		if (texId == -1) return;
		GL30.glDeleteTextures(texId);
		texId = -1;
	}
}
