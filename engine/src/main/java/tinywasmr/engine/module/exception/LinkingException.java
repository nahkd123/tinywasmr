package tinywasmr.engine.module.exception;

import java.io.Serial;

public class LinkingException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = -8241764492460702400L;

	public LinkingException(String message) {
		super(message);
	}

	public LinkingException(String message, Throwable cause) {
		super(message, cause);
	}

	public LinkingException() {
		super();
	}
}
