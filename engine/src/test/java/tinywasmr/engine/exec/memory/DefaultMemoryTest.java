package tinywasmr.engine.exec.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DefaultMemoryTest {
	@Test
	void testPartition() {
		DefaultMemory.partitionByPage(32767, 1, (page, pageOffset, pageCount) -> {
			assertEquals(0, page);
			assertEquals(32767, pageOffset);
			assertEquals(1, pageCount);
		});
	}
}
