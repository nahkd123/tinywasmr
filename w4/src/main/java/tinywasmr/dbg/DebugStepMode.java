package tinywasmr.dbg;

public enum DebugStepMode {
	/**
	 * <p>
	 * Step into the block if the instruction pointer points to block-like
	 * instructions, like {@code if} or {@code block} for example. If the
	 * instruction under the pointer is not block-like instruction, it will step
	 * just like {@link #NEXT} mode.
	 * </p>
	 */
	IN,
	/**
	 * <p>
	 * Step to next instruction in current block.
	 * </p>
	 */
	NEXT,
	/**
	 * <p>
	 * Step out of current block.
	 * </p>
	 */
	OUT;
}
