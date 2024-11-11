package tinywasmr.w4.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class KeybindEntry implements Keybind {
	private String name;
	private Collection<KeyCombination> defaults;
	private List<KeyCombination> configured;
	private Consumer<Integer> onTrigger;

	public KeybindEntry(String name, Consumer<Integer> onTrigger, Collection<KeyCombination> defaults) {
		this.name = name;
		this.onTrigger = onTrigger;
		this.defaults = defaults;
		this.configured = new ArrayList<>();
		this.configured.addAll(defaults);
	}

	public KeybindEntry(String name, Consumer<Integer> onTrigger, KeyCombination... defaults) {
		this(name, onTrigger, List.of(defaults));
	}

	public Collection<KeyCombination> getDefaults() { return defaults; }

	@Override
	public String name() {
		return name;
	}

	public List<KeyCombination> getConfigured() { return configured; }

	public String getKeybindNames() {
		return getConfigured().stream().map(KeyCombination::toString).collect(Collectors.joining("/"));
	}

	public void reset() {
		configured.clear();
		configured.addAll(defaults);
	}

	public void trigger(int action) {
		onTrigger.accept(action);
	}
}
