package tinywasmr.engine.module.func.extern;

import java.util.List;
import java.util.stream.Stream;

import tinywasmr.engine.exec.executor.Executor;
import tinywasmr.engine.exec.frame.FunctionFrame;
import tinywasmr.engine.exec.instance.Function;
import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.module.func.FunctionDecl;
import tinywasmr.engine.type.value.ValueType;

/**
 * <p>
 * Represent an external function declaration, also known as <em>host
 * function</em> inside WebAssembly module.
 * </p>
 * <p>
 * This external function interface is designed in a way that host function can
 * call guest function by pushing the function frame to the stack, rather than
 * calling {@link Function#exec(Object...)} directly. This allows TinyWasmR to
 * preserve the states of the machine into a file, which can be restored.
 * </p>
 * <p>
 * To implement an external function, you need to override the following
 * methods:
 * <ul>
 * <li>{@link #localVariables()} - (optional) declare local variables</li>
 * </ul>
 * </p>
 */
public interface ExternalFunctionDecl extends FunctionDecl {
	/**
	 * <p>
	 * Declare all local variables that this external function will use. By default,
	 * this method will return an empty list, indicating that there are no local
	 * variables to use.
	 * </p>
	 */
	default List<ValueType> localVariables() {
		return List.of();
	}

	default List<ValueType> allLocals() {
		return Stream.concat(type().inputs().types().stream(), localVariables().stream()).toList();
	}

	/**
	 * <p>
	 * Called on each instruction step while the function frame is being executed.
	 * Usually called by {@link Executor#step(tinywasmr.engine.exec.vm.Machine)}.
	 * </p>
	 * <p>
	 * To exit the function, call {@link Machine#exitFunction(Value[])}, which will
	 * pop the function frame, indicating the execution is finished.
	 * </p>
	 * <p>
	 * To call a function, use
	 * {@link Machine#call(Function, tinywasmr.engine.exec.value.Value[])}. To
	 * collect the results returned from the called function, on the next step,
	 * collect them with {@link FunctionFrame#popOprand()}. Although you can use
	 * {@link Function#exec(Object...)}, this method will create a new virtual
	 * machine, which may affect debugger.
	 * </p>
	 * 
	 * @param vm        The virtual machine.
	 * @param frame     The function frame that is referring to this function
	 *                  declaration.
	 * @param locals    The local variables, including the function inputs and extra
	 *                  variables declared from {@link #localVariables()}. The
	 *                  elements in this array will be preserved until you exit this
	 *                  function.
	 * @param stepIndex The current step this method is being invoked. To manually
	 *                  set step index for next step, use
	 *                  {@link FunctionFrame#setInsnIndex(int)}.
	 */
	public void onStep(Machine vm, FunctionFrame frame, Value[] locals, int stepIndex);
}
