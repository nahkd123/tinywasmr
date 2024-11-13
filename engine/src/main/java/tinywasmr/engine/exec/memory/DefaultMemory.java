package tinywasmr.engine.exec.memory;

import java.util.Arrays;

import tinywasmr.engine.module.memory.MemoryDecl;

public class DefaultMemory implements Memory {
	private MemoryDecl decl;
	private byte[][] pages;

	public DefaultMemory(MemoryDecl decl, int pages) {
		this.decl = decl;
		this.pages = new byte[pages][PAGE_SIZE];
	}

	public DefaultMemory(int pages) {
		this(null, pages);
	}

	public DefaultMemory(MemoryDecl decl) {
		this(decl, decl.type().limit().allocSize());
	}

	@Override
	public MemoryDecl declaration() {
		return decl;
	}

	@Override
	public int pageCount() {
		return pages.length;
	}

	public byte[][] getPages() { return pages; }

	@Override
	public int grow(int deltaPages) {
		if (deltaPages == 0) return pages.length;
		if (deltaPages < 0) throw new IllegalArgumentException("Delta is negative");
		int prev = pages.length;
		byte[][] newPages = new byte[deltaPages][PAGE_SIZE];
		byte[][] newRef = new byte[pages.length + deltaPages][];
		System.arraycopy(pages, 0, newRef, 0, prev);
		System.arraycopy(newPages, 0, newRef, prev, deltaPages);
		pages = newRef;
		return prev;
	}

	@FunctionalInterface
	public interface PartitionCallback {
		void partition(int page, int pageOffset, int pageCount);
	}

	public static void partitionByPage(int memoryOffset, int memoryCount, PartitionCallback callback) {
		if (memoryCount <= 0) return;
		int pageStart = memoryOffset / PAGE_SIZE;

		for (int i = pageStart; (i * PAGE_SIZE) < memoryOffset + memoryCount; i++) {
			int pageMemoryOffset = i * PAGE_SIZE;
			int rangeStart = Math.max(memoryOffset, pageMemoryOffset);
			int rangeEnd = Math.min(memoryOffset + memoryCount, pageMemoryOffset + PAGE_SIZE);
			callback.partition(i, rangeStart - pageMemoryOffset, rangeEnd - rangeStart);
		}
	}

	@Override
	public void read(int memoryOffset, byte[] target, int targetOffset, int count) {
		int pageStart = memoryOffset / PAGE_SIZE;
		partitionByPage(memoryOffset, count, (page, pageOffset, pageCount) -> {
			byte[] pageData = pages[page];
			int effectiveTargetOffset = targetOffset + PAGE_SIZE * (page - pageStart);
			System.arraycopy(pageData, pageOffset, target, effectiveTargetOffset, pageCount);
		});
	}

	@Override
	public void write(int memoryOffset, byte[] source, int sourceOffset, int count) {
		int pageStart = memoryOffset / PAGE_SIZE;
		partitionByPage(memoryOffset, count, (page, pageOffset, pageCount) -> {
			byte[] pageData = pages[page];
			int effectiveSourceOffset = sourceOffset + PAGE_SIZE * (page - pageStart);
			System.arraycopy(source, effectiveSourceOffset, pageData, pageOffset, pageCount);
		});
	}

	@Override
	public void fill(int memoryOffset, int byteVal, int count) {
		partitionByPage(memoryOffset, count, (page, pageOffset, pageCount) -> {
			Arrays.fill(pages[page], pageOffset, pageOffset + pageCount, (byte) byteVal);
		});
	}
}
