package tinywasmr.test.suite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.Test;

import tinywasmr.engine.exec.instance.DefaultInstance;
import tinywasmr.engine.exec.instance.Function;
import tinywasmr.engine.exec.instance.Instance;
import tinywasmr.parser.ParsedWasmModule;
import tinywasmr.parser.binary.BinaryModuleParser;

class BrTableTest {
	final String file = "suite/br_table_module.wasm";

	ParsedWasmModule load(String file) {
		try (InputStream stream = getClass().getClassLoader().getResourceAsStream(file)) {
			return BinaryModuleParser.parse(stream);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	Instance instance() {
		return new DefaultInstance(load(file), null);
	}

	@Test
	void testType() {
		instance().export("type-i32").asFunction().exec();
		instance().export("type-i64").asFunction().exec();
		instance().export("type-f32").asFunction().exec();
		instance().export("type-f64").asFunction().exec();
	}

	@Test
	void testTypeValue() {
		assertEquals(1, instance().export("type-i32-value").asFunction().exec());
		assertEquals(2L, instance().export("type-i64-value").asFunction().exec());
		assertEquals(3F, instance().export("type-f32-value").asFunction().exec());
		assertEquals(4D, instance().export("type-f64-value").asFunction().exec());
	}

	@Test
	void testEmpty() {
		Function f = instance().export("empty").asFunction();
		assertEquals(22, f.exec(0));
		assertEquals(22, f.exec(1));
		assertEquals(22, f.exec(11));
		assertEquals(22, f.exec(-1));
		assertEquals(22, f.exec(-100));
		assertEquals(22, f.exec(0xffffffff));
	}

	@Test
	void testEmptyValue() {
		Function f = instance().export("empty-value").asFunction();
		assertEquals(33, f.exec(0));
		assertEquals(33, f.exec(1));
		assertEquals(33, f.exec(11));
		assertEquals(33, f.exec(-1));
		assertEquals(33, f.exec(-100));
		assertEquals(33, f.exec(0xffffffff));
	}

	@Test
	void testSingleton() {
		Function f = instance().export("singleton").asFunction();
		assertEquals(22, f.exec(0));
		assertEquals(20, f.exec(1));
		assertEquals(20, f.exec(11));
		assertEquals(20, f.exec(-1));
		assertEquals(20, f.exec(-100));
		assertEquals(20, f.exec(0xffffffff));
	}

	@Test
	void testSingletonValue() {
		Function f = instance().export("singleton-value").asFunction();
		assertEquals(32, f.exec(0));
		assertEquals(33, f.exec(1));
		assertEquals(33, f.exec(11));
		assertEquals(33, f.exec(-1));
		assertEquals(33, f.exec(-100));
		assertEquals(33, f.exec(0xffffffff));
	}

	@Test
	void testMultiple() {
		Function f = instance().export("multiple").asFunction();
		assertEquals(103, f.exec(0));
		assertEquals(102, f.exec(1));
		assertEquals(101, f.exec(2));
		assertEquals(100, f.exec(3));
		assertEquals(104, f.exec(4));
		assertEquals(104, f.exec(5));
		assertEquals(104, f.exec(6));
		assertEquals(104, f.exec(10));
		assertEquals(104, f.exec(-1));
		assertEquals(104, f.exec(0xffffffff));
	}
}
