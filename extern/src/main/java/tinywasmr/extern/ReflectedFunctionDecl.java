package tinywasmr.extern;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import tinywasmr.engine.exec.instance.Instance;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.module.func.ExternalFunctionDecl;
import tinywasmr.engine.type.FunctionType;
import tinywasmr.engine.type.value.ValueType;

public class ReflectedFunctionDecl implements ExternalFunctionDecl {
	private Method method;
	private FunctionType type;
	private ValueType returnType;
	private ValueType[] paramTypes;

	public ReflectedFunctionDecl(Method method) {
		this.method = method;
		returnType = ReflectionUtils.classToValueType(method.getReturnType());
		Class<?>[] methodParams = method.getParameterTypes();
		paramTypes = new ValueType[methodParams.length];
		for (int i = 0; i < paramTypes.length; i++) paramTypes[i] = ReflectionUtils.classToValueType(methodParams[i]);
		this.type = new FunctionType(paramTypes, returnType != null
			? new ValueType[] { returnType }
			: new ValueType[0]);
	}

	public Method method() {
		return method;
	}

	@Override
	public FunctionType type() {
		return type;
	}

	@Override
	public Value[] onExec(Instance instance, Value[] params) {
		Object self = instance instanceof ReflectedInstance reflected ? reflected.object() : null;
		Object[] methodParams = new Object[paramTypes.length];
		for (int i = 0; i < methodParams.length; i++) methodParams[i] = paramTypes[i].mapToJava(params[i]);
		method.setAccessible(true);
		try {
			Object returnedValue = method.invoke(self, methodParams);
			return returnType == null ? new Value[0] : new Value[] { returnType.mapFromJava(returnedValue) };
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		} finally {
			method.setAccessible(false);
		}
	}
}
