package tinywasmr.w4.gui;

import java.util.List;

public record KeybindGroup(String name, List<Keybind> children) implements Keybind {
}
