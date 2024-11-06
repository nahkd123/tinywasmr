package tinywasmr.engine.exec;

import java.io.Serial;

public class ValidationException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = -2992198597060512396L;

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(Throwable cause) {
		super(cause);
	}

	public ValidationException() {}
}
