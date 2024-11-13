package tinywasmr.dbg.gui;

import imgui.ImGui;
import tinywasmr.engine.exec.memory.ByteArrayMemoryView;
import tinywasmr.engine.exec.memory.DefaultMemory;
import tinywasmr.engine.exec.memory.Memory;

public class MemoryViewer {
	private Memory memory;
	private int[] bytesPerRow = new int[] { 32 };
	private int[] page = new int[] { 0 };
	private int[] subpage = new int[] { 0 };

	public Memory getMemory() { return memory; }

	public void setMemory(Memory memory) {
		this.memory = memory;
		this.page[0] = 0;
		this.subpage[0] = 0;
	}

	public void memoryViewer() {
		if (memory == null) {
			ImGui.text("No memory opened.");
			return;
		}

		ImGui.sliderInt("Bytes/row", bytesPerRow, 8, 32, "%d bytes");
		ImGui.sliderInt("Page", page, 0, memory.pageCount() - 1, "Page %d");
		ImGui.sliderInt("Subpage", subpage, 0, 32, "Subpage #%d");
		ImGui.separator();

		byte[] bs;
		boolean fullMemoryView;

		if (memory instanceof DefaultMemory defaulted) {
			bs = defaulted.getPages()[page[0]];
			fullMemoryView = false;
		} else if (memory instanceof ByteArrayMemoryView view) {
			bs = view.content();
			fullMemoryView = true;
		} else {
			bs = new byte[0];
			fullMemoryView = true;
		}

		int offset = fullMemoryView ? (page[0] * Memory.PAGE_SIZE) : 0;
		int count = Math.min(bs.length - offset, Memory.PAGE_SIZE);
		int rows = 2048 / bytesPerRow[0];

		String header = " [Address] ";
		for (int col = 0; col < bytesPerRow[0]; col++) {
			header += "%02x ".formatted(col);
			if ((col & 0x7) == 0x7) header += " ";
		}
		ImGui.text(header);
		ImGui.separator();

		ImGui.beginChild("Memory View", 0f, 0f);
		for (int row = subpage[0] * rows; row <= (subpage[0] + 1) * rows; row++) {
			String content = "0x%08x ".formatted(page[0] * Memory.PAGE_SIZE + row * bytesPerRow[0]);
			boolean end = false;

			for (int col = 0; col < bytesPerRow[0]; col++) {
				int byteIndex = row * bytesPerRow[0] + col;

				if (byteIndex >= count) {
					end = true;
					break;
				}

				content += "%02x ".formatted(bs[offset + byteIndex] & 0xff);
				if ((col & 0x7) == 0x7) content += " ";
			}

			ImGui.text(content);
			if (end) break;
		}
		ImGui.endChild();
	}
}
