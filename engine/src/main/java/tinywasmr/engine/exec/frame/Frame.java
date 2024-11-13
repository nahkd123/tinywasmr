package tinywasmr.engine.exec.frame;

import java.util.List;

import tinywasmr.engine.exec.value.Value;
import tinywasmr.engine.exec.vm.Machine;
import tinywasmr.engine.type.BlockType;

/**
 * <p>
 * A frame represent a level of nesting, each contain its own operand stack. An
 * inner block can't access to outer block's operand stack; you have to use
 * locals to get access to them.
 * </p>
 * <p>
 * <b>Frame types:</b>
 * <ul>
 * <li>Function frame: Function frames are pushed into frame stack by calling
 * the function. These frames are for handling return instructions (return
 * instruction will attempts to pop all block frames until it reaches the
 * function frame).</li>
 * <li>Block frame: Block frames are pushed into frame stack by entering blocks.
 * These frames are for handling branching instructions. Branching instructions
 * should never attempt to branch past the function frame (runtime validation
 * can be disabled to allow this to happen).</li>
 * <li>External frame: External frames are frames that are bounds to code belong
 * to environment. These frames are for collecting the return values from
 * executing the function in module. This frame will always be the first frame
 * in virtual machine and can't be popped.</li>
 * </ul>
 * </p>
 * <p>
 * <b>The {@code br|br_if|br_table index} instruction:</b> These instruction
 * basically put all operands in current operand stack to results, pop
 * {@code index + 1} frames from frame stack and push to new current operands
 * stack the values from results.
 * </p>
 * <p>
 * <b>Returning:</b> Returning is similar to branching, except it will branches
 * until it hit the function frame.
 * </p>
 */
public interface Frame {
	/**
	 * <p>
	 * Get the current values in operand stack. Normally this will be represented as
	 * stack data structure, but it is {@link List} to imply that the operand stack
	 * is an ordered list (first value pushed to stack is represented as value in
	 * 1st position, second push at 2nd position and so on).
	 * </p>
	 * <p>
	 * Typically the returned list is not modifiable; you must use
	 * {@link #pushOperand(Value)} or {@link #popOprand()} to modify the operand
	 * stack.
	 * </p>
	 * 
	 * @return The values in operand stack.
	 */
	List<Value> getOperandStack();

	/**
	 * <p>
	 * Push value to operand stack.
	 * </p>
	 */
	void pushOperand(Value value);

	/**
	 * <p>
	 * Pop and return value from the top of operand stack.
	 * </p>
	 */
	Value popOprand();

	Value peekOperand();

	/**
	 * <p>
	 * Get the current step of this frame. The step index will be stored when saving
	 * the machine states.
	 * </p>
	 */
	int getStep();

	/**
	 * <p>
	 * Set the current step of this frame.
	 * </p>
	 */
	void setStep(int index);

	/**
	 * <p>
	 * Increase the step of this frame by 1.
	 * </p>
	 */
	default void incStep() {
		setStep(getStep() + 1);
	}

	/**
	 * <p>
	 * Check whether the execution of this frame is finished. Finished frame will be
	 * popped from frame stack by executor.
	 * </p>
	 */
	boolean isFrameFinished();

	/**
	 * <p>
	 * Branch this frame. If the frame is function or block, it will pop the frame.
	 * If the frame is a loop, it will reset the step index to 0 (equivalent of
	 * {@code continue;} in most programming languages).
	 * </p>
	 */
	void branchThis();

	/**
	 * <p>
	 * Get the result types to push to parent frame when this frame is branched.
	 * When branching or returning, the instruction will pop exactly N values from
	 * the stack, pop the frames then push those values to the parent frame.
	 * </p>
	 */
	BlockType getBranchResultTypes();

	void executeStep(Machine vm);

	/**
	 * <p>
	 * Modify the state of this frame after execution. The default implementation is
	 * increase the step index by 1, but instruction like {@code loop} may reset the
	 * step index back to 0 when {@link #nextStep()} is called after
	 * {@link #branchThis()}.
	 * </p>
	 */
	default void nextStep() {
		incStep();
	}
}
