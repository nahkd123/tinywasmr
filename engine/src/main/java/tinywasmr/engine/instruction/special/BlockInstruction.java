package tinywasmr.engine.instruction.special;

import java.util.List;
import java.util.Optional;

import tinywasmr.engine.execution.context.ExecutionContext;
import tinywasmr.engine.instruction.Instruction;
import tinywasmr.engine.module.type.TypeEnum;

public interface BlockInstruction extends Instruction {
	@Override
	default void execute(ExecutionContext ctx) {
		throw new RuntimeException("Can't be executed directly, please use Executor");
	}

	public TypeEnum getReturnType(); // block returnType {...}

	public Optional<List<Instruction>> getBodyBasedOnContext(ExecutionContext context);

	public List<Instruction> getPrimaryBody();

	public Optional<List<Instruction>> getSecondaryBody();

	public String getBlockName();

	public static TypeEnum returnTypeFromBinaryId(int id) {
		return switch (id) {
		case 0x7F -> TypeEnum.I32;
		case 0x7E -> TypeEnum.I64;
		case 0x7D -> TypeEnum.F32;
		case 0x7C -> TypeEnum.F64;
		default -> TypeEnum.VOID;
		};
	}
}
