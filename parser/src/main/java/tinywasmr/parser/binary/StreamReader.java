package tinywasmr.parser.binary;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class StreamReader {
	public static int readInt32LE(InputStream stream) throws IOException {
		byte[] bs = stream.readNBytes(4);
		if (bs.length != 4) throw new EOFException("Expected to read 4 bytes but EOF found");
		return (bs[0] & 0xff) | (bs[1] & 0xff) << 8 | (bs[2] & 0xff) << 16 | (bs[3] & 0xff) << 24;
	}

	public static int readUint32Var(InputStream stream) throws IOException {
		int result = 0;
		int shift = 0;

		while (true) {
			int b = stream.read();
			if (b == -1) break;

			result |= (b & 0x7f) << shift;
			if ((b & 0x80) == 0) break;
			shift += 7;
		}

		return result;
	}

	public static long readUint64Var(InputStream stream) throws IOException {
		long result = 0;
		long shift = 0;

		while (true) {
			int b = stream.read();
			if (b == -1) break;

			result |= (b & 0x7f) << shift;
			if ((b & 0x80) == 0) break;
			shift += 7L;
		}

		return result;
	}

	public static int readSint32Var(InputStream stream) throws IOException {
		int result = 0;
		int shift = 0;
		int b = 0;

		do {
			int b2 = stream.read();
			if (b2 == -1) break;

			b = b2;
			result |= (b & 0x7f) << shift;
			shift += 7;
		} while ((b & 0x80) != 0);

		if ((shift < 32) && (b & 0x40) != 0) result |= (~0 << shift);
		return result;
	}

	public static long readSint64Var(InputStream stream) throws IOException {
		long result = 0;
		long shift = 0;
		int b = 0;

		do {
			int b2 = stream.read();
			if (b2 == -1) break;

			b = b2;
			result |= (b & 0x7f) << shift;
			shift += 7L;
		} while ((b & 0x80) != 0);

		if ((shift < 64) && (b & 0x40) != 0) result |= (~0L << shift);
		return result;
	}

	public static float readFloat32(InputStream stream) throws IOException {
		byte[] bs = stream.readNBytes(4);
		if (bs.length != 4) throw new EOFException("Expected to read 4 bytes but EOF found");
		int i = (bs[0] & 0xff) << 24 | (bs[1] & 0xff) << 16 | (bs[2] & 0xff) << 8 | (bs[3] & 0xff);
		return Float.intBitsToFloat(i);
	}

	public static double readFloat64(InputStream stream) throws IOException {
		byte[] bs = stream.readNBytes(8);
		if (bs.length != 8) throw new EOFException("Expected to read 8 bytes but EOF found");
		long i = (bs[0] & 0xffL) << 56L
			| (bs[1] & 0xffL) << 48L
			| (bs[2] & 0xffL) << 40L
			| (bs[3] & 0xffL) << 32L
			| (bs[4] & 0xffL) << 24L
			| (bs[5] & 0xffL) << 16L
			| (bs[6] & 0xffL) << 8L
			| (bs[7] & 0xffL);
		return Double.longBitsToDouble(i);
	}

	public static char readUtf8(InputStream stream) throws IOException {
		int ch = stream.read();
		if (ch == -1) throw new EOFException();
		if (ch < 0x80) return (char) ch;

		int next;

		if ((ch & 0b111_00000) == 0b110_00000) {
			next = 1;
			ch &= 0b000_11111;
		} else if ((ch & 0b1111_0000) == 0b1110_0000) {
			next = 2;
			ch &= 0b0000_1111;
		} else if ((ch & 0b11111_000) == 0b11110_000) {
			next = 3;
			ch &= 0b00000_111;
		} else {
			return (char) ch;
		}

		for (int i = 0; i < next; i++) {
			int b = stream.read();

			if (b == -1) {
				ch <<= 6 * (next - i - 1);
				return (char) ch;
			}

			ch <<= 6;
			ch |= b & 0b00_111111;
		}

		return (char) ch;
	}

	public static String readName(InputStream stream) throws IOException {
		int len = readUint32Var(stream);
		byte[] bs = stream.readNBytes(len);
		if (bs.length != len) throw new IOException("Expected %d bytes but only %d found".formatted(len, bs.length));
		return new String(bs, StandardCharsets.UTF_8);
	}
}
