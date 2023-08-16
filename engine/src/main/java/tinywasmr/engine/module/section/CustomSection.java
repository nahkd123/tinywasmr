package tinywasmr.engine.module.section;

public interface CustomSection extends Section {
	@Override
	default SectionType getSectionType() { return SectionType.CUSTOM; }

	public String getName();

	public byte[] getContent();
}
