package tinywasmr.dbg;

import tinywasmr.engine.exec.vm.Machine;

/**
 * <p>
 * An interface for triggering various debugging features, like manual stepping
 * for example.
 * </p>
 */
public interface DebugInterface {
	Machine getMachine();

	DebugSymbols getSymbols();

	/**
	 * <p>
	 * Check whether the current machine is running. {@code false} if paused due to
	 * manual pause or trap triggered.
	 * </p>
	 */
	boolean isRunning();

	/**
	 * <p>
	 * Pause execution, enabling methods like {@link #step(DebugStepMode)} to work.
	 * </p>
	 */
	void pause();

	/**
	 * <p>
	 * Resume execution.
	 * </p>
	 */
	void resume();

	void step(DebugStepMode mode);

	/**
	 * <p>
	 * Get the current execution speed (instructions per second). Return {@code -1}
	 * if the execution speed is unlimited.
	 * </p>
	 */
	int getExecutionSpeed();

	/**
	 * <p>
	 * Set the execution speed (instructions per second). Use {@code -1} for
	 * unlimited speed.
	 * </p>
	 */
	void setExecutionSpeed(int speed);
}
