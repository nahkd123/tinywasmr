package tinywasmr.engine.module.section;

public interface UnknownSection extends Section {
	@Override
	default SectionType getSectionType() { return SectionType.UNKNOWN; }

	public int getSectionBinaryId();

	public byte[] getSectionContent();
}
