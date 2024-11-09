package tinywasmr.w4.gui;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import imgui.ImGui;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImString;

/**
 * <p>
 * A simple file picker to pick files.
 * </p>
 */
public final class FilePicker {
	private static final int TYPE_FILE = 0;
	private static final int TYPE_FOLDER = 1;
	private static final int TYPE_UNKNOWN = 2;

	private static record FileEntry(File file, String name, int type) {
	}

	private ImString currentFolder = new ImString(1024);
	private File file, folder;
	private List<FileEntry> entries;
	private Predicate<File> filter;

	public FilePicker(File initial, Predicate<File> filter) {
		this.file = initial;
		this.filter = filter;
		setFolder(initial);
	}

	public File getFile() { return file; }

	public File getFolder() { return folder; }

	public void setFolder(File file) {
		try {
			file = file.getCanonicalFile();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		if (!file.isDirectory()) setFolder(new File(file, ".."));
		folder = this.file = file;
		currentFolder.set(folder.toString());
		refresh();
	}

	public void refresh() {
		File[] ls = folder.listFiles();
		boolean isRoot = folder.getParentFile() == null;
		entries = new ArrayList<>();
		if (!isRoot) entries.add(new FileEntry(new File(folder, ".."), "<parent folder>", TYPE_FOLDER));

		for (int i = 0; i < ls.length; i++) {
			File child = ls[i];
			if (filter != null && !filter.test(child)) continue;
			String name = child.getName();
			int type = child.isFile() ? TYPE_FILE : child.isDirectory() ? TYPE_FOLDER : TYPE_UNKNOWN;
			entries.add(new FileEntry(child, name, type));
		}
	}

	/**
	 * <p>
	 * Render GUI.
	 * </p>
	 * 
	 * @return {@code true} if user double clicked on the file. Use
	 *         {@link #getFile()} to get the double clicked file.
	 */
	public boolean imgui(float width, float listHeight) {
		boolean doubleClicked = false;

		if (ImGui.button("Refresh")) refresh();
		ImGui.sameLine();
		ImGui.setNextItemWidth(width - ImGui.calcTextSizeX("Refresh") - ImGui.calcTextSizeX("Go") - 32f);
		ImGui.inputText("##Path", currentFolder);
		ImGui.sameLine();
		if (ImGui.button("Go")) setFolder(new File(currentFolder.get()));

		if (ImGui.beginTable("FileExplorer", 3, ImGuiTableFlags.ScrollY, width, listHeight)) {
			ImGui.tableSetupColumn("Name", ImGuiTableColumnFlags.WidthStretch);
			ImGui.tableSetupColumn("D", ImGuiTableColumnFlags.WidthFixed, 20f);
			ImGui.tableSetupColumn("File Type", ImGuiTableColumnFlags.WidthFixed, 120f);
			ImGui.tableHeadersRow();

			for (FileEntry entry : entries) {
				ImGui.tableNextRow();
				ImGui.tableSetColumnIndex(0);

				if (ImGui.selectable(entry.name, file.equals(entry.file))) {
					if (file.equals(entry.file)) {
						if (entry.type == TYPE_FOLDER) setFolder(entry.file);
						doubleClicked = true;
					}

					file = entry.file;
				}

				ImGui.tableSetColumnIndex(1);
				ImGui.text(entry.type == TYPE_FOLDER ? "D" : "");

				ImGui.tableSetColumnIndex(2);
				ImGui.text(switch (entry.type) {
				case TYPE_FILE -> extractExt(entry.name);
				case TYPE_FOLDER -> "Folder";
				case TYPE_UNKNOWN -> "???";
				default -> throw new RuntimeException("Type");
				});
			}

			ImGui.endTable();
		}

		return doubleClicked;
	}

	private static String extractExt(String name) {
		String[] split = name.split("\\.");
		return split[split.length - 1];
	}
}
