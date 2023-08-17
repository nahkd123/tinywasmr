package tinywasmr.engine.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tinywasmr.engine.type.Limit;

/**
 * <p>
 * WebAssembly only have little endian mode, so we need to read all values in
 * LE.
 * </p>
 */
public interface LEDataInput {
	/**
	 * <p>
	 * Attempt to read byte from underlying stream/device.
	 * </p>
	 * 
	 * @return A value from 0 to 255, or -1 if end of stream is reached.
	 */
	public int readByte() throws IOException;

	default int readBytesTo(byte[] buffer, int offsetInBuffer, int length) throws IOException {
		int v = readByte();
		if (v == -1) return -1;
		buffer[offsetInBuffer++] = (byte) v;
		int bytesRead = 1;

		while (bytesRead < length) {
			v = readByte();
			if (v == -1) return bytesRead;
			buffer[offsetInBuffer++] = (byte) v;
			bytesRead++;
		}

		return bytesRead;
	}

	default byte[] readBytes(int length) throws IOException {
		byte[] bs = new byte[length];
		int bytesRead = 0, lastBytesRead;

		while ((lastBytesRead = readBytesTo(bs, bytesRead, length - bytesRead)) != -1) {
			bytesRead += lastBytesRead;
			if (bytesRead >= length) break;
		}

		return bs;
	}

	default long readInteger(int bits) throws IOException {
		long v = 0;
		int bits2 = 0, b;

		while (bits2 < bits) {
			b = readByte();
			if (b == -1) throw new IOException("Failed to read " + bits + "-bit LE integer (" + bits2 + ")");
			v |= ((long) b) << bits2;
			bits2 += 8;
		}

		return v;
	}

	default long readI64() throws IOException {
		return readInteger(64);
	}

	default int readI32() throws IOException {
		return (int) readInteger(32);
	}

	default float readF32() throws IOException {
		return Float.intBitsToFloat(readI32());
	}

	default double readF64() throws IOException {
		return Double.longBitsToDouble(readI64());
	}

	default long readLEB128() throws IOException {
		long v = 0, shift = 0;
		int b;

		while (true) {
			b = readByte();
			if (b == -1) return v;
			v |= (b & 0x7FL) << shift;
			if ((b & 0x80L) == 0) break;
			shift += 7L;
		}

		return v;
	}

	default <T> void readVector(IOFunction<LEDataInput, T> producer, IOConsumer<T> consumer) throws IOException {
		long length = readLEB128();
		for (long i = 0; i < length; i++) consumer.consume(producer.apply(this));
	}

	default <T> List<T> readVector(IOFunction<LEDataInput, T> producer) throws IOException {
		List<T> list = new ArrayList<>();
		long length = readLEB128();
		for (long i = 0; i < length; i++) list.add(producer.apply(this));
		return list;
	}

	default char readUTF8Char() throws IOException {
		int b = readByte();
		if (b == -1) throw new IOException("End of stream");

		long c = b;
		if ((c >> 7L) != 0b1) return (char) c;

		int charactersToRead = 0;
		if ((b & 0b11_000000) == 0b10_000000) {
			charactersToRead = 1;
			c &= 0b00_111111L;
		}
		if ((b & 0b111_00000) == 0b110_00000) {
			charactersToRead = 1;
			c &= 0b000_11111L;
		}
		if ((b & 0b1111_0000) == 0b1110_0000) {
			charactersToRead = 2;
			c &= 0b0000_1111L;
		}

		while (charactersToRead > 0) {
			b = readByte();
			if (b == -1) return (char) c;

			c <<= 6L;
			c |= b & 0b00111111; // TODO emit invalid characters
			charactersToRead--;
		}

		return (char) c;
	}

	default String readUTF8() throws IOException {
		char[] cs = new char[(int) readLEB128()];
		for (int i = 0; i < cs.length; i++) cs[i] = readUTF8Char();
		return String.valueOf(cs);
	}

	default Limit readLimit() throws IOException {
		int head = readByte();
		if (head == -1) throw new IOException("End of stream");

		long min = readLEB128();
		if (head == 0x00) return Limit.min(min);

		long max = readLEB128();
		return Limit.max(min, max);
	}
}
