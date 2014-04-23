package org.preferanto.core;

public class PreferantoException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PreferantoException() {
		super();
	}

	public PreferantoException(String message, Throwable cause) {
		super(message, cause);
	}

	public PreferantoException(String message) {
		super(message);
	}

	public PreferantoException(Throwable cause) {
		super(cause);
	}

}
