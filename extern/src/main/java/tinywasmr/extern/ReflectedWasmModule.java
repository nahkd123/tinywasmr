package tinywasmr.extern;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tinywasmr.engine.module.CustomSection;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.export.ExportDecl;
import tinywasmr.engine.module.export.FunctionExportDescription;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.module.imprt.ImportDecl;
import tinywasmr.engine.module.memory.DataSegment;
import tinywasmr.engine.module.memory.MemoryDecl;
import tinywasmr.engine.module.table.TableDecl;
import tinywasmr.extern.annotation.Export;
import tinywasmr.extern.annotation.ExportType;

public class ReflectedWasmModule<T> implements WasmModule {
	private Class<T> clazz;
	private List<FunctionDecl> functions = new ArrayList<>();
	private List<ExportDecl> exports = new ArrayList<>();

	public ReflectedWasmModule(Class<T> clazz) {
		this.clazz = clazz;

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
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	@Override
	public List<FunctionDecl> declaredFunctions() {
		return functions;
	}
}
