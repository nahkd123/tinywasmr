package tinywasmr.parser.binary;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import tinywasmr.engine.exec.instance.DefaultInstance;
import tinywasmr.engine.exec.instance.Function;
import tinywasmr.engine.exec.instance.Instance;
import tinywasmr.engine.exec.instance.SimpleImporter;
import tinywasmr.engine.exec.memory.Memory;
import tinywasmr.engine.module.func.ExternalFunctionDecl;
import tinywasmr.engine.type.value.NumberType;
import tinywasmr.parser.ParsedWasmModule;

class BinaryModuleParserTest {
	ParsedWasmModule load(String file) {
		try (InputStream stream = getClass().getClassLoader().getResourceAsStream(file)) {
			return BinaryModuleParser.parse(stream);
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

	@Test
	void testImportFunction() {
		final int answer = 42;
		AtomicInteger hits = new AtomicInteger();

		ParsedWasmModule module = load("binary/002_importfunc.wasm");
		Instance instance = new DefaultInstance(module, SimpleImporter.builder()
			.module("myModule", mod -> mod
				.addVoidFunc("printI32", NumberType.I32, i -> {
					hits.incrementAndGet();
					assertEquals(answer, i);
				})
				.addFunc("answer", NumberType.I32, () -> answer))
			.build());
		instance.export("main").asFunction().exec();
		assertEquals(1, hits.get());
	}

	@Test
	void testCallIndirect() {
		AtomicInteger hits = new AtomicInteger();
		Function hitter = new Function(null, ExternalFunctionDecl.ofVoid(hits::incrementAndGet));
		ParsedWasmModule module = load("binary/003_callindirect.wasm");
		Instance instance = new DefaultInstance(module, null);
		instance.export("main").asFunction().exec(hitter);
		assertEquals(1, hits.get());
	}

	@Test
	void testBranching() {
		ParsedWasmModule module = load("binary/004_branching.wasm");
		Instance instance = new DefaultInstance(module, null);
		Object[] result = instance.export("main").asFunction().execToArray();
		assertEquals(1, result[0]);
		assertEquals(3, result[1]);
		assertEquals(4, result[2]);
	}

	@Test
	void testMemories() {
		ParsedWasmModule module = load("binary/005_memories.wasm");
		Instance instance = new DefaultInstance(module, null);
		Memory memory = instance.export("memory").asMemory();
		memory.writeI32(0, 727);
		instance.export("main").asFunction().exec();
		assertEquals(727 + 42, memory.readI32(0));
	}

	@Test
	void testLoop() {
		AtomicInteger hits = new AtomicInteger(0);
		ParsedWasmModule module = load("binary/006_loop.wasm");
		Instance instance = new DefaultInstance(module, SimpleImporter.builder()
			.module("console", mod -> mod.<Integer>addVoidFunc("log", NumberType.I32, val -> {
				assertEquals(hits.get(), val);
				hits.incrementAndGet();
			}))
			.build());
		instance.export("main").asFunction().exec();
		assertEquals(10, hits.get());
	}
}
