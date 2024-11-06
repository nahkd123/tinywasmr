package tinywasmr.engine.type;

import java.util.List;

import tinywasmr.engine.type.value.ValueType;

public interface BlockType {
	List<ValueType> blockResults();
}
