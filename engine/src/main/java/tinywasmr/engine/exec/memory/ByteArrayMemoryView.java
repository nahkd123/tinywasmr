package tinywasmr.engine.exec.memory;

import java.util.Arrays;

import tinywasmr.engine.module.memory.MemoryDecl;

/**
 * <p>
 * Memory view of a byte array. The view is not growable: the method
 * {@link #grow(int)} will always return {@code -1}.
 * </p>
 */
public record ByteArrayMemoryView(MemoryDecl declaration, int pageCount, byte[] content) implements Memory {
	public ByteArrayMemoryView {
		if (content.length > (pageCount * PAGE_SIZE))
			throw new IllegalArgumentException("length of content is more than pageCount * 65536 (%d > %d)"
				.formatted(content.length, pageCount * PAGE_SIZE));
	}

	public ByteArrayMemoryView(MemoryDecl declaration, byte[] content) {
		this(declaration, 1 + ((content.length - 1) / PAGE_SIZE), content);
	}

	public ByteArrayMemoryView(byte[] content) {
		this(null, content);
	}

	public ByteArrayMemoryView(int pageCount, byte[] content) {
		this(null, pageCount, content);
	}

	@Override
	public int grow(int deltaPages) {
		return -1;
	}

	@Override
	public void read(int memoryOffset, byte[] target, int targetOffset, int count) {
		int rangeStart = memoryOffset;
		int rangeEnd = Math.min(content.length, memoryOffset + count);
		System.arraycopy(content, rangeStart, target, targetOffset, rangeEnd - rangeStart);
		Arrays.fill(target, targetOffset + (rangeEnd - rangeStart), targetOffset + count, (byte) 0);
	}

	@Override
	public void write(int memoryOffset, byte[] source, int sourceOffset, int count) {
		int rangeStart = memoryOffset;
		int rangeEnd = Math.min(content.length, memoryOffset + count);
		System.arraycopy(source, sourceOffset, content, rangeStart, rangeEnd - rangeStart);
	}

	@Override
	public void fill(int memoryOffset, int byteVal, int count) {
		int rangeStart = memoryOffset;
		int rangeEnd = Math.min(content.length, rangeStart + count);
		Arrays.fill(content, rangeStart, rangeEnd, (byte) byteVal);
	}
}
