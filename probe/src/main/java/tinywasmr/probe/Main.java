package tinywasmr.probe;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import tinywasmr.engine.instruction.Instructions;
import tinywasmr.engine.io.LEDataInputStream;
import tinywasmr.engine.module.ModuleParsers;
import tinywasmr.engine.module.section.CodeSection;
import tinywasmr.engine.module.section.CustomSection;
import tinywasmr.engine.module.section.FunctionsSection;
import tinywasmr.engine.module.section.ImportsSection;
import tinywasmr.engine.module.section.MemorySection;
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

		System.err.println("Loaded " + Instructions.getTotalFactories() + " instruction factories");

		try (var inStream = new FileInputStream(file)) {
			var in = new LEDataInputStream(inStream);
			var module = ModuleParsers.parse(in, true);

			System.out.println("// TinyWasmR probe tool");
			System.out.println("// File: " + file.getAbsolutePath());
			System.out.println("// Summary:");
			System.out.println("//   Sections: " + module.getAllSections().size());

			for (var section : module.getAllSections()) {
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
					for (int i = 0; i < typesSection.getTypes().size(); i++)
						System.out.println("  " + i + ": " + typesSection.getTypes().get(i));
					System.out.println("}");
				}

				if (section instanceof ImportsSection importsSection) {
					System.out.println("section imports {");
					for (var importEntry : importsSection.getImports()) System.out.println("  " + importEntry);
					System.out.println("}");
				}

				if (section instanceof FunctionsSection functionsSection) {
					System.out.println("section functions {");
					for (int i = 0; i < functionsSection.getFunctions().size(); i++)
						System.out.println("  " + i + ": " + functionsSection.getFunctions().get(i));
					System.out.println("}");
				}

				if (section instanceof MemorySection memorySection) {
					System.out.println("section memory {");

					for (int i = 0; i < memorySection.getMemorySizes().size(); i++) {
						var size = memorySection.getMemorySizes().get(i);
						System.out.println(
							"  " + i + ": " + (size.hasMax() ? ("from " + size.getMin() + " to " + size.getMax())
								: "at least " + size.getMin()));
					}

					System.out.println("}");
				}

				if (section instanceof CodeSection codeSection) {
					System.out.println("section code {");
					for (int i = 0; i < codeSection.getFunctions().size(); i++) {
						System.out
							.println("  " + i + ": " + module.getFunctionsSection().get().getFunctions().get(i) + " {");
						for (var instr : codeSection.getFunctions().get(i).getInstructions()) {
							System.out.println("    " + instr);
						}
						System.out.println("  }");
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
