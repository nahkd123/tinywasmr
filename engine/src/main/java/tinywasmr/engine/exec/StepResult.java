package tinywasmr.engine.exec;

public enum StepResult {
	/**
	 * <p>
	 * The execution is completed normally.
	 * </p>
	 */
	NORMAL,
	/**
	 * <p>
	 * The virtual machine is trapped.
	 * </p>
	 */
	TRAP;
}
