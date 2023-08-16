package tinywasmr.engine.module.v1.section;

import tinywasmr.engine.module.section.UnknownSection;

public class UnknownSectionImpl implements UnknownSection {
	private int id;
	private byte[] content;

	public UnknownSectionImpl(int id, byte[] content) {
		this.id = id;
		this.content = content;
	}

	@Override
	public int getSectionBinaryId() { return id; }

	@Override
	public byte[] getSectionContent() { return content; }

	@Override
	public String toString() {
		return "unknown(id = " + id + ", length = " + content.length + ")";
	}
}
