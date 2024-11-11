package tinywasmr.w4;

public class W4Input {
	public static final int GAMEPADS_ADDR = 0x0016;
	public static final int MOUSE_ADDR = 0x001A;

	public static final int BUTTON_1 = 0b00000001;
	public static final int BUTTON_2 = 0b00000010;
	public static final int BUTTON_ENTER = 0b00000100;
	public static final int BUTTON_UNUSED = 0b00001000;
	public static final int BUTTON_LEFT = 0b00010000;
	public static final int BUTTON_RIGHT = 0b00100000;
	public static final int BUTTON_UP = 0b01000000;
	public static final int BUTTON_DOWN = 0b10000000;

	private byte[] memory;

	public W4Input(byte[] memory) {
		this.memory = memory;
	}

	public int getGamepad(int port) {
		return memory[GAMEPADS_ADDR + port] & 0xff;
	}

	public void setGamepad(int port, int flags) {
		memory[GAMEPADS_ADDR + port] = (byte) flags;
	}

	public void setGamepad(int port, int flag, boolean state) {
		int curr = getGamepad(port);
		setGamepad(port, (curr & ~flag) | (state ? flag : 0));
	}

	public int getMouseX() { return memory[MOUSE_ADDR] & 0xff | (memory[MOUSE_ADDR + 1] & 0xff) << 8; }

	public void setMouseX(int x) {
		memory[MOUSE_ADDR] = (byte) (x & 0xff);
		memory[MOUSE_ADDR + 1] = (byte) ((x >> 8) & 0xff);
	}

	public int getMouseY() { return memory[MOUSE_ADDR + 2] & 0xff | (memory[MOUSE_ADDR + 3] & 0xff) << 8; }

	public void setMouseY(int y) {
		memory[MOUSE_ADDR + 2] = (byte) (y & 0xff);
		memory[MOUSE_ADDR + 3] = (byte) ((y >> 8) & 0xff);
	}

	public int getMouseButtons() { return memory[MOUSE_ADDR + 4] & 0xff; }

	public void setMouseButtons(int flags) { memory[MOUSE_ADDR + 4] = (byte) flags; }
}
