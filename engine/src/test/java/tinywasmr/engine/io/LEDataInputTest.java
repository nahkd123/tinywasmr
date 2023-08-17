package tinywasmr.engine.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class LEDataInputTest {
	@Test
	void testLEIntegers() throws IOException {
		var bs = new ByteArrayInputStream(new byte[] { 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x32, 0x00 });
		var io = new LEDataInput() {
			@Override
			public int readByte() throws IOException {
				return bs.read();
			}
		};

		assertEquals(0x00000101, io.readInteger(32));

		bs.reset();
		assertEquals(0x0032000000000101L, io.readI64());

		bs.reset();
		assertEquals(0x00000101, io.readI32());
		assertEquals(0x00320000, io.readI32());
	}

	@Test
	void testLEB128() throws IOException {
		var bs = new ByteArrayInputStream(new byte[] {
			0x00,
			0x42,
			(byte) 0xE5, (byte) 0x8E, 0x26
		});
		var io = new LEDataInput() {
			@Override
			public int readByte() throws IOException {
				return bs.read();
			}
		};

		assertEquals(0L, io.readLEB128Unsigned());
		assertEquals(0x42L, io.readLEB128Unsigned());
		assertEquals(0b10011000011101100101L, io.readLEB128Unsigned());
	}

	@Test
	void testUTF8String() throws IOException {
		var str = "nhà em có 9 con vịt";
		var bsIn = new ByteArrayOutputStream();
		bsIn.write(str.length());
		bsIn.write(str.getBytes(StandardCharsets.UTF_8));

		var bs = new ByteArrayInputStream(bsIn.toByteArray());
		var io = new LEDataInput() {
			@Override
			public int readByte() throws IOException {
				return bs.read();
			}
		};

		assertEquals(str, io.readUTF8());
	}
}
