package tinywasmr.engine.exec.instance;

import java.util.ArrayList;
import java.util.List;

import tinywasmr.engine.exec.value.NumberI32Value;
import tinywasmr.engine.insn.ConstInsn;
import tinywasmr.engine.insn.control.BlockInsn;
import tinywasmr.engine.insn.memory.DataInitInsn;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.func.ModuleFunctionDecl;
import tinywasmr.engine.module.memory.ActiveDataMode;
import tinywasmr.engine.module.memory.DataSegment;
import tinywasmr.engine.type.FunctionType;
import tinywasmr.engine.type.value.NumberType;
import tinywasmr.engine.type.value.ValueType;

public class InitializerFunctionDecl extends ModuleFunctionDecl {
	public static final FunctionType TYPE = new FunctionType(new ValueType[0], new ValueType[0]);

	public InitializerFunctionDecl(WasmModule module) {
		super(module, TYPE, List.of(), new ArrayList<>());

		for (DataSegment segment : module.dataSegments()) {
			if (!(segment.mode() instanceof ActiveDataMode activeMode)) continue;
			// data.init memidx (memoffset, dataoffset, count)
			body().add(new BlockInsn(NumberType.I32, activeMode.offsetExpr()));
			body().add(new ConstInsn(new NumberI32Value(0)));
			body().add(new ConstInsn(new NumberI32Value(segment.data().length)));
			body().add(new DataInitInsn(segment, activeMode.memory()));
		}
	}
}
