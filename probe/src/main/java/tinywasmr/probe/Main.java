package tinywasmr.probe;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import tinywasmr.engine.io.LEDataInputStream;
import tinywasmr.engine.module.ModuleParsers;
import tinywasmr.engine.module.section.CustomSection;
import tinywasmr.engine.module.section.TypesSection;
import tinywasmr.engine.module.section.UnknownSection;
import tinywasmr.engine.util.HexString;

public class Main {
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			System.err.println("Usage: java tinywasmr.probe.Main <path/to/module.wasm>");
			System.exit(1);
		}

		var file = new File(args[0]);
		if (!file.exists()) {
			System.err.println("File does not exists!");
			System.exit(1);
		}

		try (var inStream = new FileInputStream(file)) {
			var in = new LEDataInputStream(inStream);
			var module = ModuleParsers.parse(in, true);

			System.out.println("// TinyWasmR probe tool");
			System.out.println("// File: " + file.getAbsolutePath());
			System.out.println("// Summary:");
			System.out.println("//   Sections: " + module.getAllSections().size());

			for (var section : module.getAllSections()) {
				System.out.println();
				System.out.println("// Section: " + section.getSectionType());

				if (section instanceof UnknownSection unknownSection) {
					System.out.println("section raw(" + unknownSection.getSectionBinaryId() + ") {");
					System.out.println("  // Hex dump of section:");
					sectionHexDump(unknownSection.getSectionContent());
					System.out.println("}");
				}

				if (section instanceof CustomSection customSection) {
					System.out.println("section custom(\"" + customSection.getName() + "\") {");
					System.out.println("  // Hex dump of section:");
					sectionHexDump(customSection.getContent());
					System.out.println("}");
				}

				if (section instanceof TypesSection typesSection) {
					System.out.println("section types {");

					for (int i = 0; i < typesSection.getTypes().size(); i++) {
						var idStr = "0x" + padStringBefore(Integer.toString(i, 16), 8, "0");
						var type = typesSection.getTypes().get(i);
						System.out.println("  " + idStr + ": " + type);
					}

					System.out.println("}");
				}
			}
		}
	}

	private static void sectionHexDump(byte[] content) {
		var hexPart = "";
		var textPart = "";

		for (int i = 0; i < content.length; i++) {
			hexPart += HexString.ofByte(content[i]) + " ";
			textPart += hasASCIIVisual(content[i]) ? ((char) Byte.toUnsignedInt(content[i])) : ".";

			if (((i + 1) % 16) == 0) {
				System.out.println("  " + padStringAfter(hexPart, 48, " ") + "| " + textPart);
				hexPart = "";
				textPart = "";
			}
		}

		if (!hexPart.isEmpty()) System.out.println("  " + padStringAfter(hexPart, 48, " ") + "| " + textPart);
	}

	private static String padStringBefore(String s, int maxLength, String padWith) {
		while (s.length() < maxLength) s = padWith + s;
		return s;
	}

	private static String padStringAfter(String s, int maxLength, String padWith) {
		while (s.length() < maxLength) s += padWith;
		return s;
	}

	private static boolean hasASCIIVisual(byte b) {
		return b >= 32 && b <= 126;
	}
}
