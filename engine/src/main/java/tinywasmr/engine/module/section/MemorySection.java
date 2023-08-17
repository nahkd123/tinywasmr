package tinywasmr.engine.module.section;

import java.util.List;

import tinywasmr.engine.type.Limit;

public interface MemorySection extends Section {
	@Override
	default SectionType getSectionType() { return SectionType.MEMORY; }

	public List<Limit> getMemorySizes();
}
