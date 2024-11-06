package tinywasmr.engine.exec.instance;

import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.type.FunctionType;

public record Function(Instance instance, FunctionDecl declaration) implements Exportable {
	public FunctionType type() {
		return declaration.type();
	}
}
