package tinywasmr.engine.execution.exception;

import java.io.Serial;

public class TrapException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = -4417420434257353626L;

	public TrapException(String message) {
		super(message);
	}

	public TrapException(String message, Throwable cause) {
		super(message, cause);
	}

	public TrapException() {
		super();
	}
}
