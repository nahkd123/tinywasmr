package tinywasmr.engine.exec.instance;

import java.util.List;

import tinywasmr.engine.module.CustomSection;
import tinywasmr.engine.module.WasmModule;

public interface ExternalInstance extends Instance {
	@Override
	default WasmModule module() {
		return null;
	}

	@Override
	default List<CustomSection> custom() {
		return List.of();
	}
}
