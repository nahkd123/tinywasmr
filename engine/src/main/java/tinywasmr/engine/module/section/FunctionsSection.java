package tinywasmr.engine.module.section;

import java.util.List;

import tinywasmr.engine.module.function.Function;

public interface FunctionsSection extends Section {
	@Override
	default SectionType getSectionType() { return SectionType.FUNCTIONS; }

	public List<Function> getFunctions();
}
