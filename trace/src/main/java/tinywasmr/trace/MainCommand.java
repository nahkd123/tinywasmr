package tinywasmr.trace;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import tinywasmr.engine.execution.ModuleInstance;
import tinywasmr.engine.io.LEDataInputStream;
import tinywasmr.engine.module.ModuleParsers;
import tinywasmr.engine.module.WasmModule;

@Command(name = "tinywasmr-trace")
public class MainCommand implements Callable<Integer> {
	@Parameters(
		paramLabel = "path/to/module.wasm",
		description = "Path to WebAssembly module")
	public File moduleFile;

	@Option(names = { "-A", "--auto-inspect" }, description = "Automatically inspect when stepping")
	public boolean autoInspect = true;

	@Override
	public Integer call() throws Exception {
		if (!moduleFile.exists()) {
			System.err.println("Failed to load module at path: " + moduleFile.getAbsolutePath());
			return 1;
		}

		System.out.println("(trace) Loading module: " + moduleFile.getAbsolutePath());
		WasmModule module;
		try (var inStream = new FileInputStream(moduleFile)) {
			module = ModuleParsers.parse(new LEDataInputStream(inStream), true);
		}

		System.out.println("(trace) Module loaded!");
		System.out.println("        Sections: " + module.getAllSections().size());

		var importsCount = module.getImportsSection().map(v -> v.getImports().size()).orElse(0);
		System.out.println("        Imports: " + importsCount);

		System.out.println("        Functions: ");
		var functions = module.getFunctionsSection().map(v -> v.getFunctions()).orElse(Collections.emptyList());
		for (int i = 0; i < functions.size(); i++) {
			var f = functions.get(i);
			int fid = i + importsCount;
			System.out.println("        " + fid + " :: " + f.getSignature());
		}

		System.out.println("(trace) Creating trace session...");
		var instance = new ModuleInstance(module);
		var session = new TraceSession(this, instance);

		try (var stdinScanner = new Scanner(System.in)) {
			System.out.println("(trace) Ready.");
			System.out.println("(trace) Press Enter to step.");
			while (true) { session.sendCommand(stdinScanner.nextLine()); }
		}
	}
}
