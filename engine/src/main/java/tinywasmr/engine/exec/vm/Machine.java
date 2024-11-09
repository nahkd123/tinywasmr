package tinywasmr.engine.exec.vm;

import java.util.List;

import tinywasmr.engine.exec.frame.ExternalFrame;
import tinywasmr.engine.exec.frame.Frame;
import tinywasmr.engine.exec.frame.FunctionFrame;
import tinywasmr.engine.exec.instance.Function;
import tinywasmr.engine.exec.trap.ModuleTrap;
import tinywasmr.engine.exec.trap.Trap;
import tinywasmr.engine.exec.value.Value;

/**
 * <p>
 * Represent WebAssembly virtual machine.
 * </p>
 */
public interface Machine {
	List<Frame> getFrameStack();

	/**
	 * <p>
	 * Get the external frame, which is the most-bottom frame in the stack. This
	 * frame's purpose is to collect the results from the first executed function.
	 * </p>
	 * 
	 * @return
	 */
	ExternalFrame getExternalFrame();

	/**
	 * <p>
	 * Peek the current frame. This method never returns {@code null} because you
	 * can't pop the external frame.
	 * </p>
	 */
	Frame peekFrame();

	FunctionFrame peekFunctionFrame();

	void pushFrame(Frame frame);

	default FunctionFrame call(Function function, Value[] parameters) {
		FunctionFrame frame = FunctionFrame.createCall(function, parameters);
		pushFrame(frame);
		return frame;
	}

	/**
	 * <p>
	 * Exit currently executing function and optionally push results to next current
	 * operand stack.
	 * </p>
	 * 
	 * @param results The results to push. Use {@code null} to push none.
	 * @return The function frame that has been popped from stack.
	 */
	default FunctionFrame exitFunction(Value[] results) {
		if (peekFrame() == getExternalFrame())
			throw new IllegalStateException("Cannot exit: no function is being executed");
		while (!(popFrame() instanceof FunctionFrame funcFrame));
		if (results != null) for (Value val : results) peekFrame().pushOperand(val);
		return funcFrame;
	}

	Frame popFrame();

	/**
	 * <p>
	 * Get the current trap. Returns {@code null} if the virtual machine is not
	 * trapped.
	 * </p>
	 */
	Trap getTrap();

	/**
	 * <p>
	 * Trap this virtual machine, or remove trap from it.
	 * </p>
	 * 
	 * @param trap The trap to set.
	 */
	void setTrap(Trap trap);

	/**
	 * <p>
	 * Check whether to validate during runtime. By default, the virtual machine
	 * will make no attempts to validate the code. If validation failed, it will
	 * trap the virtual machine with {@link ModuleTrap}.
	 * </p>
	 */
	boolean hasRuntimeValidation();

	void setRuntimeValidation(boolean validation);
}
