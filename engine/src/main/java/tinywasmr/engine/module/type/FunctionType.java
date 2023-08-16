package tinywasmr.engine.module.type;

import java.util.List;

public interface FunctionType extends Type {
	@Override
	default TypeEnum getTypeEnum() { return TypeEnum.FUNCTION; }

	public List<Type> getArgumentTypes();

	public List<Type> getReturnTypes();
}
