package tinywasmr.extern;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import tinywasmr.extern.annotation.Export;

class ReflectedInstanceTest {
	@Test
	void testSimple() {
		AtomicInteger hits = new AtomicInteger();

		class MyInstance {
			@Export(exportAs = "main")
			void main() {
				hits.incrementAndGet();
			}
		}

		ReflectedWasmModule<MyInstance> module = new ReflectedWasmModule<>(MyInstance.class);
		ReflectedInstance<MyInstance> instance = module.instanceOf(new MyInstance());
		instance.export("main").asFunction().exec();
		assertEquals(1, hits.get());
	}
}
