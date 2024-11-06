package tinywasmr.engine.exec;

import java.io.Serial;

public class TrapException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 3200343184793717184L;

	public TrapException(String message, Throwable cause) {
		super(message, cause);
	}

	public TrapException(String message) {
		super(message);
	}

	public TrapException(Throwable cause) {
		super(cause);
	}

	public TrapException() {}
}
