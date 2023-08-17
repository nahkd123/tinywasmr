package tinywasmr.trace;

import java.io.File;
import java.io.FileInputStream;
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

	@Option(names = { "-T", "--trap-pause" }, description = "Pause VM executation on trap")
	public boolean pauseOnTraps = false;

	@Override
	public Integer call() throws Exception {
		if (!moduleFile.exists()) {
			System.err.println("Failed to load module at path: " + moduleFile.getAbsolutePath());
			return 1;
		}

		System.out.println("(trace) Loading module...");
		WasmModule module;
		try (var inStream = new FileInputStream(moduleFile)) {
			module = ModuleParsers.parse(new LEDataInputStream(inStream), true);
		}

		System.out.println("(trace) Creating trace session...");
		var instance = new ModuleInstance(module);
		// TODO
		return 0;
	}
}
