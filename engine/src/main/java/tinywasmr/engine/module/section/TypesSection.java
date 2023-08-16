package tinywasmr.engine.module.section;

import java.util.List;

import tinywasmr.engine.module.type.Type;

public interface TypesSection extends Section {
	@Override
	default SectionType getSectionType() { return SectionType.TYPES; }

	public List<Type> getTypes();
}
