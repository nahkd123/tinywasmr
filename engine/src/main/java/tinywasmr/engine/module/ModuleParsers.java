package tinywasmr.engine.module;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import tinywasmr.engine.io.LEDataInput;
import tinywasmr.engine.util.HexString;

public class ModuleParsers {
	private static final Map<Integer, ModuleParser> PARSERS = new TreeMap<>();
	private static final byte[] SIGNATURE = new byte[] { 0x00, 0x61, 0x73, 0x6d };

	private static void addParser(ModuleParser parser) {
		PARSERS.put(parser.getFormatVersion(), parser);
	}

	static {
		addParser(new tinywasmr.engine.module.v1.ModuleParserImpl());
	}

	public static WasmModule parse(LEDataInput in, boolean includeSignature) throws IOException {
		if (includeSignature) {
			var signature = in.readBytes(4);
			if (!Arrays.equals(signature, SIGNATURE))
				throw new IOException("Invaild signature: 0x" + HexString.ofBytes(signature));
		}

		var version = in.readI32();
		var parser = PARSERS.get(version);
		if (parser == null) throw new IOException("Unimplemented binary version: " + version);

		return parser.parse(in);
	}
}
