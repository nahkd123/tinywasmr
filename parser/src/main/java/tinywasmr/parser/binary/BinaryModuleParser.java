package tinywasmr.parser.binary;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import tinywasmr.engine.module.CustomSection;
import tinywasmr.engine.module.export.ExportDecl;
import tinywasmr.engine.module.export.FunctionExportDescription;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.func.ImportFunctionDecl;
import tinywasmr.engine.module.func.ModuleFunctionDecl;
import tinywasmr.engine.module.imprt.FunctionImportDescription;
import tinywasmr.engine.module.imprt.ImportDecl;
import tinywasmr.engine.module.table.ModuleTableDecl;
import tinywasmr.engine.module.table.TableDecl;
import tinywasmr.engine.type.FunctionType;
import tinywasmr.engine.type.GlobalType;
import tinywasmr.engine.type.Limit;
import tinywasmr.engine.type.MemoryType;
import tinywasmr.engine.type.Mutablity;
import tinywasmr.engine.type.ResultType;
import tinywasmr.engine.type.TableType;
import tinywasmr.engine.type.value.NumberType;
import tinywasmr.engine.type.value.RefType;
import tinywasmr.engine.type.value.ValueType;
import tinywasmr.engine.type.value.VectorType;
import tinywasmr.engine.util.Logger;
import tinywasmr.engine.util.VoidLogger;
import tinywasmr.parser.ParsedWasmModule;
import tinywasmr.parser.binary.imprt.BinaryImport;

public class BinaryModuleParser {
	private static final byte[] SIGNATURE = new byte[] { 0x00, 0x61, 0x73, 0x6d };
	private static final int VERSION = 1;

	private Logger logger;
	private boolean ignoreCustomSections;

	private List<CustomSection> custom;
	private List<FunctionType> types;
	private int[] functions;
	private TableType[] tables;
	private BinaryImport[] imports;
	private BinaryExport[] exports;
	private BinaryFunctionBody[] code;

	/**
	 * <p>
	 * Create a new parser to parse binary module.
	 * </p>
	 * 
	 * @param logger               The logger to log while parsing.
	 * @param ignoreCustomSections Whether to ignore custom sections. Custom
	 *                             sections are usually filled with debugging or
	 *                             third party extension information, like source
	 *                             map or add-on info.
	 */
	public BinaryModuleParser(Logger logger, boolean ignoreCustomSections) {
		this.logger = logger != null ? logger : new VoidLogger();
		this.ignoreCustomSections = ignoreCustomSections;
		reset();
	}

	/**
	 * <p>
	 * Create a new parser to parse binary module with default options and no
	 * logging.
	 * </p>
	 * <p>
	 * The following options are used when you create parser using this constructor:
	 * <ul>
	 * <li>ignoreCustomSections set to {@code false}</li>
	 * </ul>
	 * </p>
	 */
	public BinaryModuleParser() {
		this(new VoidLogger(), false);
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
		imports = new BinaryImport[0];
		exports = new BinaryExport[0];
		code = new BinaryFunctionBody[0];
	}

	public Logger getLogger() { return logger; }

	public boolean isIgnoreCustomSections() { return ignoreCustomSections; }

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
		logger.verbose("begin parsing module");
		parseHeader(stream);
		while (stream.available() > 0) parseSection(stream);
		logger.verbose("end parsing module");
		return buildModule();
	}

	public void parseHeader(InputStream stream) throws IOException {
		logger.verbose("begin parsing header");
		byte[] signature = stream.readNBytes(4);
		if (!Arrays.equals(SIGNATURE, signature)) throw new IOException("The first 4 bytes must be '\\0asm'");

		int version = StreamReader.readInt32LE(stream);
		if (VERSION != version) throw new IOException("Unsupported WebAssembly binary version %d".formatted(version));
		logger.verbose("version is %d", version);
		logger.verbose("end parsing header");
	}

	public void parseSection(InputStream stream) throws IOException {
		logger.verbose("begin parsing section");
		SectionHeader header = SectionParser.parseSectionHeader(this, stream);

		// It is worth mentioning that some schizo compilers may choose to put anything
		// before type section, like imports or code for example.
		// @formatter:off
		switch (header.id()) {
		case 0x00: custom.add(SectionParser.parseCustomSection(this, header.size(), stream)); break;
		case 0x01: types = SectionParser.parseTypeSection(this, header.size(), stream); break;
		case 0x02: imports = SectionParser.parseImportSection(this, header.size(), stream); break;
		case 0x03: functions = SectionParser.parseFunctionSection(this, header.size(), stream); break;
		case 0x04: tables = SectionParser.parseTableSection(this, header.size(), stream); break;
		case 0x05: throw new RuntimeException("0x05 memory section not implemented");
		case 0x06: throw new RuntimeException("0x06 global section not implemented");
		case 0x07: exports = SectionParser.parseExportSection(this, header.size(), stream); break;
		case 0x08: throw new RuntimeException("0x08 start section not implemented");
		case 0x09: throw new RuntimeException("0x09 element section not implemented");
		case 0x0A: code = SectionParser.parseCodeSection(this, header.size(), stream); break;
		case 0x0B: throw new RuntimeException("0x0B data section not implemented");
		case 0x0C: throw new RuntimeException("0x0C data count section not implemented");
		// @formatter:on
		default:
			logger.warn("Section with ID 0x%02x is not implemented", header.id());
			if (header.size() == 0) throw new IOException("Section 0x%02x not implemented, can't skip (guessing size)"
				.formatted(header.id()));
			stream.skipNBytes(header.size());
			break;
		}

		logger.verbose("end parsing section");
	}

	public ValueType parseValueType(int id) throws IOException {
		return switch (id) {
		case 0x7f -> NumberType.I32;
		case 0x7e -> NumberType.I64;
		case 0x7d -> NumberType.F32;
		case 0x7c -> NumberType.F64;
		case 0x7b -> VectorType.V128;
		case 0x70 -> RefType.FUNC;
		case 0x6f -> RefType.EXTERN;
		default -> null;
		};
	}

	public ValueType parseValueType(InputStream stream) throws IOException {
		int id = stream.read();
		if (id == -1) throw new EOFException();
		ValueType valueType = parseValueType(id);
		if (valueType == null) throw new IOException("Value type not implemented: 0x%02x".formatted(id));
		return valueType;
	}

	public ResultType parseResultType(InputStream stream) throws IOException {
		int len = StreamReader.readUint32Var(stream);
		List<ValueType> types = new ArrayList<>();
		for (int i = 0; i < len; i++) types.add(parseValueType(stream));
		return new ResultType(types);
	}

	public FunctionType parseFunctionType(int id, InputStream stream) throws IOException {
		if (id != 0x60) return null;
		ResultType params = parseResultType(stream);
		ResultType results = parseResultType(stream);
		return new FunctionType(params, results);
	}

	public FunctionType parseFunctionType(InputStream stream) throws IOException {
		int id = stream.read();
		if (id == -1) throw new EOFException();
		FunctionType functionType = parseFunctionType(id, stream);
		if (functionType == null)
			throw new IOException("Function type ID must be 0x60, but 0x%02x found".formatted(id));
		return functionType;
	}

	public Limit parseLimit(InputStream stream) throws IOException {
		int type = stream.read();
		return switch (type) {
		case 0x00 -> new Limit(StreamReader.readUint32Var(stream));
		case 0x01 -> new Limit(StreamReader.readUint32Var(stream), StreamReader.readUint32Var(stream));
		case -1 -> throw new EOFException();
		default -> throw new IOException("Limit type not implemented: 0x%02x".formatted(type));
		};
	}

	public MemoryType parseMemoryType(InputStream stream) throws IOException {
		return new MemoryType(parseLimit(stream));
	}

	public TableType parseTableType(InputStream stream) throws IOException {
		ValueType valueType = parseValueType(stream); // TODO replace with parseRefType()
		if (!(valueType instanceof RefType refType))
			throw new IOException("Expected RefType, but %s found".formatted(valueType));
		Limit limit = parseLimit(stream);
		return new TableType(limit, refType);
	}

	public GlobalType parseGlobalType(InputStream stream) throws IOException {
		ValueType valueType = parseValueType(stream);
		int mutablityId = stream.read();
		Mutablity mutablity = switch (mutablityId) {
		case 0x00 -> Mutablity.CONST;
		case 0x01 -> Mutablity.VAR;
		case -1 -> throw new EOFException();
		default -> throw new IOException("Mutablity type not implemented: 0x%02x".formatted(mutablityId));
		};
		return new GlobalType(mutablity, valueType);
	}

	public Object parseBlockType(InputStream stream) throws IOException {
		int id = StreamReader.readSint32Var(stream);
		if (id >= 0) return id;
		id = 0x80 + id;
		if (id == 0x40) return new ResultType(List.of());
		ValueType valueType = parseValueType(id);
		if (valueType != null) return valueType;
		throw new IOException("Block result opcode not implemented: 0x%02x".formatted(id));
	}

	public ParsedWasmModule buildModule() throws IOException {
		logger.verbose("building module");

		ParsedWasmModule module = new ParsedWasmModule();
		List<FunctionDecl> functions = new ArrayList<>();
		List<TableDecl> tables = new ArrayList<>();
		BinaryIndicesView indicesView = new BinaryIndicesView(types, tables, functions);

		// Resolving imports
		List<ImportDecl> imports = Stream.of(this.imports)
			.map(binary -> new ImportDecl(binary.module(), binary.name(), binary.description().build(indicesView)))
			.toList();
		for (ImportDecl imp : imports) {
			if (imp.description() instanceof FunctionImportDescription funcImport) {
				functions.add(new ImportFunctionDecl(module, funcImport.type(), imp));
			} else {
				throw new RuntimeException("Not implemented: %s".formatted(imp.getClass()));
			}
		}

		// Resolving declared tables
		List<ModuleTableDecl> moduleTables = Stream.of(this.tables)
			.map(type -> new ModuleTableDecl(module, type))
			.toList();
		tables.addAll(moduleTables);

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
			code[i].body().forEach(b -> decl.body().add(b.build(indicesView)));
		}

		// Finalize
		module.declaredImports().addAll(imports);
		module.declaredTables().addAll(tables);
		module.declaredFunctions().addAll(functions);
		module.declaredExports().addAll(exports);

		logger.verbose("done building module");
		return module;
	}
}
