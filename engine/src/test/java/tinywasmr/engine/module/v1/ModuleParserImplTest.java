package tinywasmr.engine.module.v1;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import tinywasmr.engine.io.LEDataInput;

class ModuleParserImplTest {
	@Test
	void testParsing() throws IOException {
		// fail("Not yet implemented");
		assertTrue(true);

		var parser = new ModuleParserImpl();
		var inStream = ModuleParserImplTest.class.getClassLoader().getResourceAsStream("addTwo.wasm");
		var in = new LEDataInput() {
			@Override
			public int readByte() throws IOException {
				return inStream.read();
			}
		};

		in.readBytes(8); // Skip header
		System.out.println(parser.parse(in).getAllSections());
	}
}
