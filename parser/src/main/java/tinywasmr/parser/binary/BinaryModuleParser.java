package tinywasmr.parser.binary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import tinywasmr.engine.module.CustomSection;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.export.ExportDecl;
import tinywasmr.engine.module.export.FunctionExportDescription;
import tinywasmr.engine.module.export.MemoryExportDescription;
import tinywasmr.engine.module.export.TableExportDescription;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.func.ImportFunctionDecl;
import tinywasmr.engine.module.func.ModuleFunctionDecl;
import tinywasmr.engine.module.imprt.FunctionImportDescription;
import tinywasmr.engine.module.imprt.ImportDecl;
import tinywasmr.engine.module.imprt.MemoryImportDescription;
import tinywasmr.engine.module.imprt.TableImportDescription;
import tinywasmr.engine.module.memory.ImportMemoryDecl;
import tinywasmr.engine.module.memory.MemoryDecl;
import tinywasmr.engine.module.memory.ModuleMemoryDecl;
import tinywasmr.engine.module.table.ImportTableDecl;
import tinywasmr.engine.module.table.ModuleTableDecl;
import tinywasmr.engine.module.table.TableDecl;
import tinywasmr.engine.type.FunctionType;
import tinywasmr.engine.type.MemoryType;
import tinywasmr.engine.type.TableType;
import tinywasmr.parser.ParsedWasmModule;
import tinywasmr.parser.binary.imprt.BinaryImport;

/**
 * <p>
 * A binary module parser to parse from a stream of bytes into
 * {@link WasmModule}.
 * </p>
 * 
 * @see #parse(InputStream)
 * @see #parse(byte[])
 * @see #parseModule(InputStream)
 */
public class BinaryModuleParser {
	private static final byte[] SIGNATURE = new byte[] { 0x00, 0x61, 0x73, 0x6d };
	private static final int VERSION = 1;

	// Options
	private boolean ignoreCustomSections;
	private boolean ignoreUnknownSections;

	private List<CustomSection> custom;
	private List<FunctionType> types;
	private int[] functions;
	private TableType[] tables;
	private MemoryType[] memories;
	private BinaryImport[] imports;
	private BinaryExport[] exports;
	private BinaryFunctionBody[] code;
	private Map<Integer, List<byte[]>> unknowns;

	/**
	 * <p>
	 * Create a new parser to parse binary module.
	 * </p>
	 * 
	 * @param ignoreCustomSections  Whether to ignore custom sections. Custom
	 *                              sections are usually filled with debugging or
	 *                              third party extension information, like source
	 *                              map or add-on info.
	 * @param ignoreUnknownSections Whether to ignore unknown sections. Unknown
	 *                              sections will not be included in
	 *                              {@link WasmModule}, but you can get them from
	 *                              this parser after calling
	 *                              {@link #parseModule(InputStream)} by using.
	 */
	public BinaryModuleParser(boolean ignoreCustomSections, boolean ignoreUnknownSections) {
		this.ignoreCustomSections = ignoreCustomSections;
		this.ignoreUnknownSections = ignoreUnknownSections;
		reset();
	}

	/**
	 * <p>
	 * Create a new parser to parse binary module with default options.
	 * </p>
	 * <p>
	 * The following options are used when you create parser using this constructor:
	 * <ul>
	 * <li>ignoreCustomSections set to {@code false}</li>
	 * <li>ignoreUnknownSections set to {@code true}</li>
	 * </ul>
	 * </p>
	 */
	public BinaryModuleParser() {
		this(false, true);
	}

	/**
	 * <p>
	 * Reset the states of this parser. Must use before you call
	 * {@link #parseModule(InputStream)} to parse different module.
	 * </p>
	 */
	public void reset() {
		custom = new ArrayList<>();
		types = List.of();
		functions = new int[0];
		tables = new TableType[0];
		memories = new MemoryType[0];
		imports = new BinaryImport[0];
		exports = new BinaryExport[0];
		code = new BinaryFunctionBody[0];
		unknowns = new HashMap<>();
	}

	public boolean isIgnoreCustomSections() { return ignoreCustomSections; }

	public boolean isIgnoreUnknownSections() { return ignoreUnknownSections; }

	/**
	 * <p>
	 * Parse module from bytes stream and return {@link ParsedWasmModule} for
	 * consumption. When reusing this parser, you must call {@link #reset()} between
	 * 2 modules.
	 * </p>
	 * 
	 * @param stream The stream to read from.
	 * @return The parsed module.
	 * @throws IOException if an IO error occurred or this parser got confused while
	 *                     reading.
	 */
	public ParsedWasmModule parseModule(InputStream stream) throws IOException {
		parseHeader(stream);
		while (stream.available() > 0) parseSection(stream);
		return buildModule();
	}

	public void parseHeader(InputStream stream) throws IOException {
		byte[] signature = stream.readNBytes(4);
		if (!Arrays.equals(SIGNATURE, signature)) throw new IOException("The first 4 bytes must be '\\0asm'");

		int version = StreamReader.readInt32LE(stream);
		if (VERSION != version) throw new IOException("Unsupported WebAssembly binary version %d".formatted(version));
	}

	public void parseSection(InputStream stream) throws IOException {
		SectionHeader header = SectionParser.parseSectionHeader(stream);

		// It is worth mentioning that some schizo compilers may choose to put anything
		// before type section, like imports or code for example.
		// @formatter:off
		switch (header.id()) {
		case 0x00: custom.add(SectionParser.parseCustomSection(header.size(), stream, ignoreCustomSections)); break;
		case 0x01: types = SectionParser.parseTypeSection(header.size(), stream); break;
		case 0x02: imports = SectionParser.parseImportSection(header.size(), stream); break;
		case 0x03: functions = SectionParser.parseFunctionSection(header.size(), stream); break;
		case 0x04: tables = SectionParser.parseTableSection(header.size(), stream); break;
		case 0x05: memories = SectionParser.parseMemorySection(header.size(), stream); break;
		case 0x06: throw new RuntimeException("0x06 global section not implemented");
		case 0x07: exports = SectionParser.parseExportSection(header.size(), stream); break;
		case 0x08: throw new RuntimeException("0x08 start section not implemented");
		case 0x09: throw new RuntimeException("0x09 element section not implemented");
		case 0x0A: code = SectionParser.parseCodeSection(header.size(), stream); break;
		case 0x0B: throw new RuntimeException("0x0B data section not implemented");
		case 0x0C: throw new RuntimeException("0x0C data count section not implemented");
		// @formatter:on
		default:
			if (header.size() == 0) throw new IOException("Section 0x%02x not implemented, can't skip (guessing size)"
				.formatted(header.id()));
			if (ignoreUnknownSections) stream.skipNBytes(header.size());
			else getUnknown(header.id()).add(stream.readNBytes(header.size()));
			break;
		}
	}

	public ParsedWasmModule buildModule() throws IOException {
		ParsedWasmModule module = new ParsedWasmModule();
		List<FunctionDecl> functions = new ArrayList<>();
		List<TableDecl> tables = new ArrayList<>();
		List<MemoryDecl> memories = new ArrayList<>();
		BinaryModuleLayout indicesView = new BinaryModuleLayout(types, tables, memories, functions);

		// Resolving imports
		List<ImportDecl> imports = Stream.of(this.imports)
			.map(binary -> new ImportDecl(binary.module(), binary.name(), binary.description().build(indicesView)))
			.toList();
		for (ImportDecl imp : imports) {
			if (imp.description() instanceof FunctionImportDescription funcImport) {
				functions.add(new ImportFunctionDecl(module, funcImport.type(), imp));
			} else if (imp.description() instanceof TableImportDescription tableImport) {
				tables.add(new ImportTableDecl(module, tableImport.type(), imp));
			} else if (imp.description() instanceof MemoryImportDescription memoryImport) {
				memories.add(new ImportMemoryDecl(module, memoryImport.type(), imp));
			} else {
				throw new RuntimeException("Not implemented: %s".formatted(imp.getClass()));
			}
		}

		// Resolving declared tables
		List<ModuleTableDecl> moduleTables = Stream.of(this.tables)
			.map(type -> new ModuleTableDecl(module, type))
			.toList();
		tables.addAll(moduleTables);

		// Resolving declared memories
		List<ModuleMemoryDecl> moduleMemories = Stream.of(this.memories)
			.map(type -> new ModuleMemoryDecl(module, type))
			.toList();
		memories.addAll(moduleMemories);

		// Resolving declared functions
		List<ModuleFunctionDecl> moduleFunctions = IntStream.of(this.functions)
			.mapToObj(types::get)
			.map(type -> new ModuleFunctionDecl(module, type, new ArrayList<>(), new ArrayList<>()))
			.toList();
		functions.addAll(moduleFunctions);

		// Resolving exports
		List<ExportDecl> exports = Stream.of(this.exports)
			.map(binary -> new ExportDecl(binary.name(), switch (binary.type()) {
			case BinaryExport.TYPE_FUNC -> new FunctionExportDescription(functions.get(binary.index()));
			case BinaryExport.TYPE_TABLE -> new TableExportDescription(tables.get(binary.index()));
			case BinaryExport.TYPE_MEM -> new MemoryExportDescription(memories.get(binary.index()));
			default -> throw new RuntimeException("Export type not implemented: 0x%02x".formatted(binary.type()));
			}))
			.toList();

		// Resolving function bodies (unwrapping instructions)
		if (code.length != moduleFunctions.size()) throw new IOException("Number of functions in code section does not"
			+ "match with number of functions in function section (%d != %d)".formatted(
				code.length,
				moduleFunctions.size()));

		for (int i = 0; i < code.length; i++) {
			ModuleFunctionDecl decl = moduleFunctions.get(i);
			decl.extraLocals().addAll(code[i].locals());
			code[i].body().forEach(b -> decl.body().add(b.build(indicesView)));
		}

		// Finalize
		module.declaredImports().addAll(imports);
		module.declaredTables().addAll(tables);
		module.declaredMemories().addAll(memories);
		module.declaredFunctions().addAll(functions);
		module.declaredExports().addAll(exports);
		return module;
	}

	/**
	 * <p>
	 * Get unknown section data from this parser. Unknown sections are usually
	 * ignored.
	 * </p>
	 * <p>
	 * This will always return an empty list if the section is known.
	 * </p>
	 * 
	 * @param typeId The binary ID of the section.
	 * @return A list of section chunks.
	 */
	public List<byte[]> getUnknown(int typeId) {
		return unknowns.computeIfAbsent(typeId, $ -> new ArrayList<>());
	}

	/**
	 * <p>
	 * Get a set of unknown section IDs.
	 * </p>
	 * 
	 * @return A set of unknown section IDs.
	 */
	public Set<Integer> getUnknownIds() { return unknowns.keySet(); }

	/**
	 * <p>
	 * Parse WebAssembly binary module from byte stream to {@link ParsedWasmModule}.
	 * This static method will create a new {@link BinaryModuleParser} with default
	 * options, parse the module, return it and then discard the parser states.
	 * </p>
	 * 
	 * @param stream A byte stream to parse.
	 * @return A parsed WebAssembly module, ready to be used.
	 * @throws IOException if I/O operation error occurred or end of stream reached
	 *                     unexpectedly.
	 */
	public static ParsedWasmModule parse(InputStream stream) throws IOException {
		return new BinaryModuleParser().parseModule(stream);
	}

	/**
	 * <p>
	 * Parse WebAssembly binary module from byte array to {@link ParsedWasmModule}.
	 * This static method will wrap the byte array into
	 * {@link ByteArrayInputStream}, then pass it to {@link #parse(InputStream)}.
	 * </p>
	 * 
	 * @param bs A byte array to parse.
	 * @return A parsed WebAssembly module, ready to be used.
	 * @throws UncheckedIOException if end of array reached unexpectedly.
	 */
	public static ParsedWasmModule parse(byte[] bs) {
		try (ByteArrayInputStream stream = new ByteArrayInputStream(bs)) {
			return parse(stream);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
