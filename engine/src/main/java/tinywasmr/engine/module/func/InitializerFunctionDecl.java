package tinywasmr.engine.module.func;

import java.util.ArrayList;
import java.util.List;

import tinywasmr.engine.exec.value.NumberI32Value;
import tinywasmr.engine.insn.ConstInsn;
import tinywasmr.engine.insn.control.BlockInsn;
import tinywasmr.engine.insn.control.CallInsn;
import tinywasmr.engine.insn.memory.MemoryInitInsn;
import tinywasmr.engine.insn.table.TableInitInsn;
import tinywasmr.engine.insn.variable.GlobalInsn;
import tinywasmr.engine.insn.variable.GlobalInsnType;
import tinywasmr.engine.module.WasmModule;
import tinywasmr.engine.module.global.GlobalDecl;
import tinywasmr.engine.module.global.ModuleGlobalDecl;
import tinywasmr.engine.module.memory.ActiveDataMode;
import tinywasmr.engine.module.memory.DataSegment;
import tinywasmr.engine.module.table.ActiveElementMode;
import tinywasmr.engine.module.table.ElementSegment;
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
			body().add(new MemoryInitInsn(segment, activeMode.memory()));
		}

		for (ElementSegment segment : module.elementSegments()) {
			if (!(segment.mode() instanceof ActiveElementMode activeMode)) continue;
			body().add(new BlockInsn(NumberType.I32, activeMode.offsetExpr()));
			body().add(new ConstInsn(new NumberI32Value(0)));
			body().add(new ConstInsn(new NumberI32Value(segment.init().size())));
			body().add(new TableInitInsn(activeMode.table(), segment));
		}

		for (GlobalDecl global : module.declaredGlobals()) {
			if (!(global instanceof ModuleGlobalDecl decl)) continue;
			body().add(new BlockInsn(decl.type().valType(), decl.init()));
			body().add(new GlobalInsn(GlobalInsnType.SET, decl));
		}

		if (module.startFunction() != null) body().add(new CallInsn(module.startFunction()));
	}
}
