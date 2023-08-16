package tinywasmr.engine.module;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import tinywasmr.engine.io.LEDataInputStream;

class ModuleParsersTest {
	@Test
	void testVersion1() throws IOException {
		// fail("Not yet implemented");
		assertTrue(true);

		var inStream = ModuleParsersTest.class.getClassLoader().getResourceAsStream("v1_addtwo.wasm");
		var in = new LEDataInputStream(inStream);

		var module = ModuleParsers.parse(in, true);
		assertNotNull(module);
		System.out.println(module.getAllSections());
	}
}
