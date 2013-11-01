package com.eldest.proxy.simple;

public class ProxyException extends RuntimeException {
	private static final long serialVersionUID = 1389469323889613235L;

	public ProxyException() {
		super();
	}
	
	public ProxyException(Throwable cause) {
		super(cause);
	}
	
	public ProxyException(String message) {
		super(message);
	}
	
	public ProxyException(String message, Throwable cause) {
		super(message, cause);
	}
}
