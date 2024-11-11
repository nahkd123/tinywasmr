package tinywasmr.w4.gui;

public record KeyCombination(int key, int modifiers) {
	public KeyCombination(int key) {
		this(key, 0);
	}

	@Override
	public final String toString() {
		return W4Keybinds.nameOfGlfw(key, modifiers);
	}
}
