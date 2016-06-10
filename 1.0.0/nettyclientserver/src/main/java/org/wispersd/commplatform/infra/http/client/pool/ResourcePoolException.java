package org.wispersd.commplatform.infra.http.client.pool;

public class ResourcePoolException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7098430403935269762L;

	public ResourcePoolException() {
		super();
	}

	public ResourcePoolException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ResourcePoolException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourcePoolException(String message) {
		super(message);
	}

	public ResourcePoolException(Throwable cause) {
		super(cause);
	}

	
}
