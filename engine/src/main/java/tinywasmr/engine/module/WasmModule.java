package tinywasmr.engine.module;

import java.util.List;

import tinywasmr.engine.module.section.Section;

public interface WasmModule {
	public List<Section> getAllSections();
}
