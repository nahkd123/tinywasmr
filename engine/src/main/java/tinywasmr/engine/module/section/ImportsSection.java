package tinywasmr.engine.module.section;

import java.util.List;

import tinywasmr.engine.module.importing.Import;

public interface ImportsSection extends Section {
	@Override
	default SectionType getSectionType() { return SectionType.IMPORTS; }

	public List<Import> getImports();
}
