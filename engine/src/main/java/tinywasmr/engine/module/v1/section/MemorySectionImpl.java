package tinywasmr.engine.module.v1.section;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tinywasmr.engine.io.LEDataInput;
import tinywasmr.engine.module.section.MemorySection;
import tinywasmr.engine.type.Limit;

public class MemorySectionImpl implements MemorySection {
	private List<Limit> memorySizes;

	public MemorySectionImpl(List<Limit> memorySizes) {
		this.memorySizes = memorySizes;
	}

	public MemorySectionImpl(LEDataInput in) throws IOException {
		long count = in.readLEB128();
		memorySizes = new ArrayList<>();
		for (int i = 0; i < count; i++) memorySizes.add(in.readLimit());
	}

	@Override
	public List<Limit> getMemorySizes() { return Collections.unmodifiableList(memorySizes); }
}
