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

class Memory000Test {
	final String file = "suite/memory_000.wasm";

	ParsedWasmModule load(String file) {
		try (InputStream stream = getClass().getClassLoader().getResourceAsStream(file)) {
			return BinaryModuleParser.parse(stream);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	Instance instance() {
		DefaultInstance i = new DefaultInstance(load(file), null);
		i.initialize();
		return i;
	}

	@Test
	void testData() {
		Function f = instance().export("data").asFunction();
		assertEquals(1, f.exec());
	}

	@Test
	void testCast() {
		Function f = instance().export("cast").asFunction();
		assertEquals(42D, f.exec());
	}

	@Test
	void testI32Load() {
		assertEquals(-1, instance().export("i32_load8_s").asFunction().exec(-1));
		assertEquals(255, instance().export("i32_load8_u").asFunction().exec(-1));
		assertEquals(-1, instance().export("i32_load16_s").asFunction().exec(-1));
		assertEquals(65535, instance().export("i32_load16_u").asFunction().exec(-1));

		assertEquals(100, instance().export("i32_load8_s").asFunction().exec(100));
		assertEquals(200, instance().export("i32_load8_u").asFunction().exec(200));
		assertEquals(20000, instance().export("i32_load16_s").asFunction().exec(20000));
		assertEquals(40000, instance().export("i32_load16_u").asFunction().exec(40000));

		assertEquals(0x43, instance().export("i32_load8_s").asFunction().exec(0xfedc6543));
		assertEquals(0xffffffef, instance().export("i32_load8_s").asFunction().exec(0x3456cdef));
		assertEquals(0x43, instance().export("i32_load8_u").asFunction().exec(0xfedc6543));
		assertEquals(0xef, instance().export("i32_load8_u").asFunction().exec(0x3456cdef));

		assertEquals(0x6543, instance().export("i32_load16_s").asFunction().exec(0xfedc6543));
		assertEquals(0xffffcdef, instance().export("i32_load16_s").asFunction().exec(0x3456cdef));
		assertEquals(0x6543, instance().export("i32_load16_u").asFunction().exec(0xfedc6543));
		assertEquals(0xcdef, instance().export("i32_load16_u").asFunction().exec(0x3456cdef));
	}

	@Test
	void testI64Load() {
		assertEquals(-1L, instance().export("i64_load8_s").asFunction().exec(-1L));
		assertEquals(255L, instance().export("i64_load8_u").asFunction().exec(-1L));
		assertEquals(-1L, instance().export("i64_load16_s").asFunction().exec(-1L));
		assertEquals(65535L, instance().export("i64_load16_u").asFunction().exec(-1L));
		assertEquals(-1L, instance().export("i64_load32_s").asFunction().exec(-1L));
		assertEquals(4294967295L, instance().export("i64_load32_u").asFunction().exec(-1L));

		assertEquals(100L, instance().export("i64_load8_s").asFunction().exec(100L));
		assertEquals(200L, instance().export("i64_load8_u").asFunction().exec(200L));
		assertEquals(20000L, instance().export("i64_load16_s").asFunction().exec(20000L));
		assertEquals(40000L, instance().export("i64_load16_u").asFunction().exec(40000L));
		assertEquals(20000L, instance().export("i64_load32_s").asFunction().exec(20000L));
		assertEquals(40000L, instance().export("i64_load32_u").asFunction().exec(40000L));

		assertEquals(0x43L, instance().export("i64_load8_s").asFunction().exec(0xfedcba9856346543L));
		assertEquals(0xffffffffffffffefL, instance().export("i64_load8_s").asFunction().exec(0x3456436598bacdefL));
		assertEquals(0x43L, instance().export("i64_load8_u").asFunction().exec(0xfedcba9856346543L));
		assertEquals(0xefL, instance().export("i64_load8_u").asFunction().exec(0x3456436598bacdefL));

		assertEquals(0x6543L, instance().export("i64_load16_s").asFunction().exec(0xfedcba9856346543L));
		assertEquals(0xffffffffffffcdefL, instance().export("i64_load16_s").asFunction().exec(0x3456436598bacdefL));
		assertEquals(0x6543L, instance().export("i64_load16_u").asFunction().exec(0xfedcba9856346543L));
		assertEquals(0xcdefL, instance().export("i64_load16_u").asFunction().exec(0x3456436598bacdefL));

		assertEquals(0x56346543L, instance().export("i64_load32_s").asFunction().exec(0xfedcba9856346543L));
		assertEquals(0xffffffff98bacdefL, instance().export("i64_load32_s").asFunction().exec(0x3456436598bacdefL));
		assertEquals(0x56346543L, instance().export("i64_load32_u").asFunction().exec(0xfedcba9856346543L));
		assertEquals(0x98bacdefL, instance().export("i64_load32_u").asFunction().exec(0x3456436598bacdefL));
	}
}
