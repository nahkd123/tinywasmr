package tinywasmr.parser.binary;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.Test;

import tinywasmr.engine.exec.instance.DefaultInstance;
import tinywasmr.engine.exec.instance.Instance;
import tinywasmr.engine.util.SystemLogger;
import tinywasmr.parser.ParsedWasmModule;

class BinaryModuleParserTest {
	ParsedWasmModule load(String file) {
		try (InputStream stream = getClass().getClassLoader().getResourceAsStream(file)) {
			BinaryModuleParser parser = new BinaryModuleParser(new SystemLogger(true), false);
			return parser.parseModule(stream);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Test
	void testAddTwo() {
		ParsedWasmModule module = load("binary/001_addtwo.wasm");
		Instance instance = new DefaultInstance(module, null);
		assertEquals(3, instance.export("addTwo").asFunction().exec(1, 2));
	}
}
