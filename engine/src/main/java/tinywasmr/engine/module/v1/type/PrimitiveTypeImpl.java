package tinywasmr.engine.module.v1.type;

import tinywasmr.engine.module.type.Type;
import tinywasmr.engine.module.type.TypeEnum;

public class PrimitiveTypeImpl implements Type {
	public static final PrimitiveTypeImpl I32 = new PrimitiveTypeImpl(TypeEnum.I32);
	public static final PrimitiveTypeImpl I64 = new PrimitiveTypeImpl(TypeEnum.I64);
	public static final PrimitiveTypeImpl F32 = new PrimitiveTypeImpl(TypeEnum.F32);
	public static final PrimitiveTypeImpl F64 = new PrimitiveTypeImpl(TypeEnum.F64);
	public static final PrimitiveTypeImpl V128 = new PrimitiveTypeImpl(TypeEnum.V128);

	private TypeEnum typeEnum;

	private PrimitiveTypeImpl(TypeEnum typeEnum) {
		this.typeEnum = typeEnum;
	}

	@Override
	public TypeEnum getTypeEnum() { return typeEnum; }

	@Override
	public String toString() {
		return typeEnum.toString().toLowerCase();
	}
}
