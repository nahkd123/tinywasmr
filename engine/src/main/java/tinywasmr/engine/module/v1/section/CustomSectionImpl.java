package tinywasmr.engine.module.v1.section;

import java.io.IOException;

import tinywasmr.engine.io.LEDataInput;
import tinywasmr.engine.io.LEDataInputWithCounter;
import tinywasmr.engine.module.section.CustomSection;

public class CustomSectionImpl implements CustomSection {
	private String name;
	private byte[] content;

	public CustomSectionImpl(String name, byte[] content) {
		this.name = name;
		this.content = content;
	}

	public CustomSectionImpl(LEDataInput in, int size) throws IOException {
		var nameWithLength = LEDataInputWithCounter.countBytes(in, s -> s.readUTF8());
		this.name = nameWithLength.getB();
		this.content = in.readBytes(size - nameWithLength.getA());
	}

	@Override
	public String getName() { return name; }

	@Override
	public byte[] getContent() { return content; }

	@Override
	public String toString() {
		return "custom(name = " + name + ", length = " + content.length + ")";
	}
}
