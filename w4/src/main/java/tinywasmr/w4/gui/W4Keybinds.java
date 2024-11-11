package tinywasmr.w4.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import imgui.ImGui;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiWindowFlags;

public class W4Keybinds {
	private List<Keybind> root;
	private Set<KeybindEntry> flattened = new HashSet<>();
	private Map<KeyCombination, KeybindEntry> keycombToKeybind = new HashMap<>();
	private KeybindEntry configuringKeybind = null;
	private int configuringEntry = -1;
	private KeyCombination currentKey = null;

	public W4Keybinds(List<Keybind> root) {
		this.root = root;
		for (Keybind kb : root) flatten(kb);
		recalculate();
	}

	public KeybindEntry getConfiguringKeybind() { return configuringKeybind; }

	public void glfwEvent(int key, int modifiers, int action) {
		currentKey = new KeyCombination(key, modifiers);

		if (action == GLFW.GLFW_RELEASE) {
			if (configuringEntry == -1) configuringKeybind.getConfigured().add(currentKey);
			else configuringKeybind.getConfigured().set(configuringEntry, currentKey);
			configuringKeybind = null;
			configuringEntry = -1;
		}
	}

	private void flatten(Keybind kb) {
		if (kb instanceof KeybindGroup group) for (Keybind child : group.children()) flatten(child);
		if (kb instanceof KeybindEntry entry) flattened.add(entry);
	}

	public Set<KeybindEntry> getFlattened() { return flattened; }

	public void recalculate() {
		keycombToKeybind.clear();

		for (KeybindEntry entry : flattened) {
			for (KeyCombination combination : entry.getConfigured()) keycombToKeybind.put(combination, entry);
		}
	}

	public KeybindEntry keybindFromGlfw(int key, int modifiers) {
		return keycombToKeybind.get(new KeyCombination(key, modifiers));
	}

	public void imgui() {
		if (ImGui.button("Reset all")) for (Keybind kb : root) reset(kb);
		imgui(root);
	}

	public void imgui(List<Keybind> children) {
		for (Keybind kb : children) {
			if (kb instanceof KeybindGroup group) imgui(group);
			if (kb instanceof KeybindEntry entry) imgui(entry);
		}
	}

	private static void reset(Keybind keybind) {
		if (keybind instanceof KeybindGroup group) {
			for (Keybind child : group.children()) reset(child);
			return;
		}

		if (keybind instanceof KeybindEntry entry) entry.reset();
	}

	public void imgui(KeybindGroup group) {
		if (ImGui.collapsingHeader(group.name())) {
			ImGui.indent();
			ImGui.pushID(group.name());
			if (ImGui.button("Reset all in this group")) for (Keybind kb : group.children()) reset(kb);
			imgui(group.children());
			ImGui.popID();
			ImGui.unindent();
		}
	}

	public void imgui(KeybindEntry entry) {
		if (ImGui.collapsingHeader(entry.name())) {
			ImGui.indent();
			ImGui.pushID(entry.name());
			if (ImGui.button("Reset")) entry.reset();
			boolean openPopup = false;

			if (ImGui.beginTable("Keys", 3)) {
				ImGui.tableSetupColumn("#", ImGuiTableColumnFlags.WidthFixed, 20f);
				ImGui.tableSetupColumn("Key combination", ImGuiTableColumnFlags.WidthStretch);
				ImGui.tableSetupColumn("Action", ImGuiTableColumnFlags.WidthFixed, 80f);
				ImGui.tableHeadersRow();

				for (int i = 0; i < entry.getConfigured().size(); i++) {
					ImGui.pushID(i);
					ImGui.tableNextRow();
					ImGui.tableSetColumnIndex(0);
					ImGui.text(Integer.toString(i + 1));
					ImGui.tableSetColumnIndex(1);

					if (ImGui.selectable(entry.getConfigured().get(i).toString())) {
						configuringKeybind = entry;
						configuringEntry = i;
						openPopup = true;
					}

					ImGui.tableSetColumnIndex(2);
					if (ImGui.selectable("Delete")) entry.getConfigured().remove(i);
					ImGui.popID();
				}

				boolean add;
				ImGui.tableNextRow();
				ImGui.tableSetColumnIndex(1);
				add = ImGui.selectable("Add combination");
				ImGui.tableSetColumnIndex(2);
				add |= ImGui.selectable("Add");
				ImGui.endTable();

				if (add) {
					configuringKeybind = entry;
					configuringEntry = -1;
					openPopup = true;
				}
			}

			if (openPopup) ImGui.openPopup("Configure Keybind");
			if (ImGui.beginPopupModal("Configure Keybind", ImGuiWindowFlags.AlwaysAutoResize)) {
				if (configuringKeybind != null) {
					ImGui.text("Configuring %s".formatted(configuringKeybind.name()));

					if (configuringEntry == -1) {
						ImGui.text("Adding key combination");
					} else {
						ImGui.text("Editing %s".formatted(configuringKeybind.getConfigured().get(configuringEntry)));
					}

					ImGui.text(currentKey == null ? "Press the keys..." : currentKey.toString());
					ImGui.newLine();
					if (ImGui.button("Cancel")) ImGui.closeCurrentPopup();
				} else {
					ImGui.closeCurrentPopup();
				}

				ImGui.end();
			}

			ImGui.popID();
			ImGui.unindent();
		}
	}

	public static String nameOfGlfwKey(int key) {
		return switch (key) {
		case GLFW.GLFW_KEY_0 -> "0";
		case GLFW.GLFW_KEY_1 -> "1";
		case GLFW.GLFW_KEY_2 -> "2";
		case GLFW.GLFW_KEY_3 -> "3";
		case GLFW.GLFW_KEY_4 -> "4";
		case GLFW.GLFW_KEY_5 -> "5";
		case GLFW.GLFW_KEY_6 -> "6";
		case GLFW.GLFW_KEY_7 -> "7";
		case GLFW.GLFW_KEY_8 -> "8";
		case GLFW.GLFW_KEY_9 -> "9";
		default -> "GLFW:%02x".formatted(key);
		};
	}

	public static String nameOfGlfwModifiers(int mods) {
		String out = "";
		if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) out += "Ctrl + ";
		if ((mods & GLFW.GLFW_MOD_ALT) != 0) out += "Alt + ";
		if ((mods & GLFW.GLFW_MOD_SHIFT) != 0) out += "Shift + ";
		return out;
	}

	public static String nameOfGlfw(int key, int mods) {
		return "%s%s".formatted(nameOfGlfwModifiers(mods), nameOfGlfwKey(key));
	}
}
