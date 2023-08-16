package tinywasmr.engine.module.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.section.Section;

public class WasmModuleImpl implements WasmModule {
	private List<Section> sections = new ArrayList<>();

	@Override
	public List<Section> getAllSections() { return Collections.unmodifiableList(sections); }

	public List<Section> getModifiableSectionsList() { return sections; }
}
