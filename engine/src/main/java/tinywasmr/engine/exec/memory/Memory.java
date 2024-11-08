package tinywasmr.engine.exec.memory;

import tinywasmr.engine.exec.instance.Exportable;
import tinywasmr.engine.module.memory.MemoryDecl;

public interface Memory extends Exportable {
	/**
	 * <p>
	 * Number of bytes per page. This is a constant value that is defined in
	 * WebAssembly specification.
	 * </p>
	 * 
	 * @see <a href=
	 *      "https://webassembly.github.io/spec/core/exec/runtime.html#page-size">WebAssembly
	 *      Specification - Core - Runtime - Memory Instances</a>
	 */
	static int PAGE_SIZE = 65536;

	MemoryDecl declaration();

	/**
	 * <p>
	 * The number of pages in this memory instance.
	 * </p>
	 */
	int pageCount();

	/**
	 * <p>
	 * The number of bytes this memory instance is holding. This will always be a
	 * multiple of {@link #PAGE_SIZE}.
	 * </p>
	 */
	default int byteSize() {
		return pageCount() * PAGE_SIZE;
	}

	/**
	 * <p>
	 * Grow this memory instance by specified number of pages and return the
	 * previous page count.
	 * </p>
	 * 
	 * @param deltaPages The number of pages to grow.
	 * @return Previous page count, or {@code -1} if allocation failed.
	 */
	int grow(int deltaPages);

	/**
	 * <p>
	 * Copy data from this memory instance to target byte array.
	 * </p>
	 * 
	 * @param memoryOffset The offset in this memory instance, in bytes.
	 * @param target       The target byte array to collect the results.
	 * @param targetOffset The offset in target array.
	 * @param count        The number of bytes to read.
	 * @throws IndexOutOfBoundsException if {@code memoryOffset + count} is larger
	 *                                   than byte size of this memory instance, or
	 *                                   {@code targetOffset + count} is larger than
	 *                                   the length of target array.
	 */
	void read(int memoryOffset, byte[] target, int targetOffset, int count);

	default byte[] read(int memoryOffset, int count) {
		byte[] bs = new byte[count];
		read(memoryOffset, bs, 0, count);
		return bs;
	}

	/**
	 * <p>
	 * Copy data from source byte array to this memory instance.
	 * </p>
	 * 
	 * @param memoryOffset The offset in this memory instance, in bytes.
	 * @param source       The source byte array to copy from.
	 * @param sourceOffset The offset in source byte array.
	 * @param count        The number of bytes to write.
	 * @throws IndexOutOfBoundsException if {@code memoryOffset + count} is larger
	 *                                   than byte size of this memory instance, or
	 *                                   {@code sourceOffset + count} is larger than
	 *                                   the length of source array.
	 */
	void write(int memoryOffset, byte[] source, int sourceOffset, int count);

	default void write(int memoryOffset, byte[] source) {
		write(memoryOffset, source, 0, source.length);
	}

	default int readS8(int memoryOffset) {
		return read(memoryOffset, 1)[0];
	}

	default int readU8(int memoryOffset) {
		return read(memoryOffset, 1)[0] & 0xff;
	}

	default int readS16(int memoryOffset) {
		return (short) readU16(memoryOffset);
	}

	default int readU16(int memoryOffset) {
		byte[] bs = read(memoryOffset, 2);
		return bs[0] & 0xff | (bs[1] & 0xff) << 8;
	}

	default int readI32(int memoryOffset) {
		byte[] bs = read(memoryOffset, 4);
		return bs[0] & 0xff | (bs[1] & 0xff) << 8 | (bs[2] & 0xff) << 16 | (bs[3] & 0xff) << 24;
	}

	default long readS32(int memoryOffset) {
		return readI32(memoryOffset);
	}

	default long readU32(int memoryOffset) {
		return readI32(memoryOffset) & 0xFFFFFFFFL;
	}

	default long readI64(int memoryOffset) {
		byte[] bs = read(memoryOffset, 8);
		return bs[0] & 0xffL
			| (bs[1] & 0xffL) << 8L
			| (bs[2] & 0xffL) << 16L
			| (bs[3] & 0xffL) << 24L
			| (bs[4] & 0xffL) << 32L
			| (bs[5] & 0xffL) << 40L
			| (bs[6] & 0xffL) << 48L
			| (bs[7] & 0xffL) << 56L;
	}

	default float readF32(int memoryOffset) {
		byte[] bs = read(memoryOffset, 4);
		int i = (bs[0] & 0xff) << 24 | (bs[1] & 0xff) << 16 | (bs[2] & 0xff) << 8 | bs[3] & 0xff;
		return Float.intBitsToFloat(i);
	}

	default double readF64(int memoryOffset) {
		byte[] bs = read(memoryOffset, 8);
		long l = (bs[0] & 0xffL) << 56L
			| (bs[1] & 0xffL) << 48L
			| (bs[2] & 0xffL) << 40L
			| (bs[3] & 0xffL) << 32L
			| (bs[4] & 0xffL) << 24L
			| (bs[5] & 0xffL) << 16L
			| (bs[6] & 0xffL) << 8L
			| bs[7] & 0xffL;
		return Double.longBitsToDouble(l);
	}

	default void writeI8(int memoryOffset, int value) {
		write(memoryOffset, new byte[] { (byte) value });
	}

	default void writeI16(int memoryOffset, int value) {
		write(memoryOffset, new byte[] {
			(byte) value,
			(byte) (value >> 8),
		});
	}

	default void writeI32(int memoryOffset, int value) {
		write(memoryOffset, new byte[] {
			(byte) value,
			(byte) (value >> 8),
			(byte) (value >> 16),
			(byte) (value >> 24),
		});
	}

	default void writeI64(int memoryOffset, long value) {
		write(memoryOffset, new byte[] {
			(byte) value,
			(byte) (value >> 8L),
			(byte) (value >> 16L),
			(byte) (value >> 24L),
			(byte) (value >> 32L),
			(byte) (value >> 40L),
			(byte) (value >> 48L),
			(byte) (value >> 56L),
		});
	}

	default void writeF32(int memoryOffset, float value) {
		int i = Float.floatToRawIntBits(value);
		write(memoryOffset, new byte[] {
			(byte) (i >> 24),
			(byte) (i >> 16),
			(byte) (i >> 8),
			(byte) i,
		});
	}

	default void writeF64(int memoryOffset, double value) {
		long i = Double.doubleToRawLongBits(value);
		write(memoryOffset, new byte[] {
			(byte) (i >> 56L),
			(byte) (i >> 48L),
			(byte) (i >> 40L),
			(byte) (i >> 32L),
			(byte) (i >> 24L),
			(byte) (i >> 16L),
			(byte) (i >> 8L),
			(byte) i,
		});
	}
}
