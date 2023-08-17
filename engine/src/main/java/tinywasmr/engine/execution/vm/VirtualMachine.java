package tinywasmr.engine.execution.vm;

import java.io.IOException;
import java.util.List;

import tinywasmr.engine.execution.stack.MachineStack;
import tinywasmr.engine.execution.value.ValueHolder;
import tinywasmr.engine.io.SeekableLEDataInput;

public interface VirtualMachine {
	public void execute(SeekableLEDataInput code, MachineStack stack, List<ValueHolder> locals) throws IOException;
}
