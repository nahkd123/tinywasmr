package tinywasmr.engine.module.v1.section;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tinywasmr.engine.io.LEDataInput;
import tinywasmr.engine.module.section.TypesSection;
import tinywasmr.engine.module.type.Type;
import tinywasmr.engine.module.v1.ModuleParserImpl;

public class TypesSectionImpl implements TypesSection {
	private List<Type> types;

	public TypesSectionImpl(List<Type> types) {
		this.types = types;
	}

	public TypesSectionImpl(LEDataInput in, ModuleParserImpl parser) throws IOException {
		long count = in.readLEB128Unsigned();
		types = new ArrayList<>();
		for (int i = 0; i < count; i++) types.add(parser.parseType(in));
	}

	@Override
	public List<Type> getTypes() { return Collections.unmodifiableList(types); }

	@Override
	public String toString() {
		var types = String.join(", ", this.types.stream().map(v -> v.toString()).toArray(String[]::new));
		return "types(" + types + ")";
	}
}
