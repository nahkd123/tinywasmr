package tinywasmr.engine.module.section;

import java.util.List;

import tinywasmr.engine.module.function.FunctionCode;

public interface CodeSection extends Section {
	@Override
	default SectionType getSectionType() { return SectionType.CODE; }

	public List<FunctionCode> getFunctions();
}
