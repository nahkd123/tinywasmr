package tinywasmr.engine.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import tinywasmr.engine.instruction.impl.LogicInstructions;
import tinywasmr.engine.io.LEByteBuffer;

class InstructionsTest {
	@Test
	void testParse() throws IOException {
		var list = Instructions.parse(new LEByteBuffer(ByteBuffer.wrap(new byte[] {
			0x00, 0x01, 0x02
		})));

		assertEquals(LogicInstructions.UNREACHABLE, list.get(0));
		assertEquals(LogicInstructions.NOP, list.get(1));
		assertEquals(LogicInstructions.END, list.get(2));
	}
}
