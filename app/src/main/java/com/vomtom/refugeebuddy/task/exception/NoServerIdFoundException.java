package com.vomtom.refugeebuddy.task.exception;

/**
 * Created by Thomas on 21.02.2016.
 */
public class NoServerIdFoundException extends Exception {

	public NoServerIdFoundException() { super(); }
	public NoServerIdFoundException(String message) { super(message); }
	public NoServerIdFoundException(String message, Throwable cause) { super(message, cause); }
	public NoServerIdFoundException(Throwable cause) { super(cause); }
}
