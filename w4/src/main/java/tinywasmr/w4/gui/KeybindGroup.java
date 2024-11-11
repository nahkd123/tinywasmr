package tinywasmr.w4.gui;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import tinywasmr.w4.W4Input;

public record KeybindGroup(String name, List<Keybind> children) implements Keybind {

	public static KeybindGroup ofGamepad(int index, GamepadInput callback, Map<Integer, List<KeyCombination>> defaults) {
		int[] flags = {
			W4Input.BUTTON_UP,
			W4Input.BUTTON_DOWN,
			W4Input.BUTTON_LEFT,
			W4Input.BUTTON_RIGHT,
			W4Input.BUTTON_1,
			W4Input.BUTTON_2,
		};
		String[] names = {
			"Button Up",
			"Button Down",
			"Button Left",
			"Button Right",
			"Button 1/X",
			"Button 2/Z"
		};
		Keybind[] entries = new Keybind[names.length];

		for (int i = 0; i < entries.length; i++) {
			final int j = i;
			entries[i] = new KeybindEntry(names[i], action -> {
				boolean state = action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT;
				callback.onInput(flags[j], state);
			}, defaults.getOrDefault(flags[i], Collections.emptyList()));
		}

		return new KeybindGroup("Gamepad #%d".formatted(index + 1), List.of(entries));
	}

	@FunctionalInterface
	public static interface GamepadInput {
		void onInput(int flag, boolean state);
	}
}
