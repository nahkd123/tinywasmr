package tinywasmr.engine.module;

import java.util.List;
import java.util.Optional;

import tinywasmr.engine.module.section.CodeSection;
import tinywasmr.engine.module.section.FunctionsSection;
import tinywasmr.engine.module.section.ImportsSection;
import tinywasmr.engine.module.section.Section;
import tinywasmr.engine.module.section.SectionType;
import tinywasmr.engine.module.section.TypesSection;

public interface WasmModule {
	public List<Section> getAllSections();

	default Optional<TypesSection> getTypesSection() {
		return getAllSections().stream()
			.filter(v -> v.getSectionType() == SectionType.TYPES)
			.findFirst()
			.map(v -> (TypesSection) v);
	}

	default Optional<ImportsSection> getImportsSection() {
		return getAllSections().stream()
			.filter(v -> v.getSectionType() == SectionType.IMPORTS)
			.findFirst()
			.map(v -> (ImportsSection) v);
	}

	default Optional<FunctionsSection> getFunctionsSection() {
		return getAllSections().stream()
			.filter(v -> v.getSectionType() == SectionType.FUNCTIONS)
			.findFirst()
			.map(v -> (FunctionsSection) v);
	}

	default Optional<CodeSection> getCodeSection() {
		return getAllSections().stream()
			.filter(v -> v.getSectionType() == SectionType.CODE)
			.findFirst()
			.map(v -> (CodeSection) v);
	}
}
