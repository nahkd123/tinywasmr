package tinywasmr.extern;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tinywasmr.engine.module.CustomSection;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.export.ExportDecl;
import tinywasmr.engine.module.export.FunctionExportDescription;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.global.GlobalDecl;
import tinywasmr.engine.module.imprt.ImportDecl;
import tinywasmr.engine.module.memory.DataSegment;
import tinywasmr.engine.module.memory.MemoryDecl;
import tinywasmr.engine.module.table.TableDecl;
import tinywasmr.extern.annotation.Export;
import tinywasmr.extern.annotation.ExportType;

public class ReflectedModule<T> implements WasmModule {
	private Class<T> clazz;
	private List<FunctionDecl> functions = new ArrayList<>();
	private List<MemoryDecl> memories = new ArrayList<>();
	private List<ExportDecl> exports = new ArrayList<>();

	public ReflectedModule(Class<T> clazz) {
		this.clazz = clazz;

		for (Field field : clazz.getDeclaredFields()) {
			Export annotation = field.getDeclaredAnnotation(Export.class);
			if (annotation == null) continue;
			String name = annotation.exportAs();
			if (name.isEmpty()) name = field.getName();
			ExportType type = annotation.value();

			if (type == ExportType.AUTO) {
				if (field.getType() == byte[].class) type = ExportType.MEMORY;
				else throw new RuntimeException("Unable to automatically detect export type for %s"
					.formatted(field));
			}

			MemoryDecl decl = switch (type) {
			case MEMORY -> new ReflectedMemoryDecl(field);
			default -> throw new RuntimeException("Unreachable");
			};

			memories.add(decl);
		}

		for (Method method : clazz.getDeclaredMethods()) {
			Export annotation = method.getDeclaredAnnotation(Export.class);
			if (annotation == null) continue;
			String name = annotation.exportAs();
			if (name.isEmpty()) name = method.getName();

			if (annotation.value() == ExportType.AUTO || annotation.value() == ExportType.FUNCTION) {
				ReflectedFunctionDecl decl = new ReflectedFunctionDecl(method);
				functions.add(decl);
				exports.add(new ExportDecl(name, new FunctionExportDescription(decl)));
			} else if (annotation.value() == ExportType.GLOBAL) {
				// TODO
				throw new RuntimeException("Table export not implemented");
			}
		}
	}

	public Class<T> clazz() {
		return clazz;
	}

	public ReflectedInstance<T> instanceOf(T object) {
		return new ReflectedInstance<>(this, object);
	}

	@Override
	public List<CustomSection> custom() {
		return Collections.emptyList();
	}

	@Override
	public List<DataSegment> dataSegments() {
		return Collections.emptyList();
	}

	@Override
	public List<ImportDecl> declaredImports() {
		return Collections.emptyList();
	}

	@Override
	public List<ExportDecl> declaredExports() {
		return exports;
	}

	@Override
	public List<TableDecl> declaredTables() {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	@Override
	public List<MemoryDecl> declaredMemories() {
		return memories;
	}

	@Override
	public List<GlobalDecl> declaredGlobals() {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	@Override
	public List<FunctionDecl> declaredFunctions() {
		return functions;
	}
}
