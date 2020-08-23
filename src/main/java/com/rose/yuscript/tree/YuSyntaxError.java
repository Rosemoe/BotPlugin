/**
 * This Java File is Created By Rose
 */
package com.rose.yuscript.tree;

/**
 * @author Rose
 *
 */
public class YuSyntaxError extends Error {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2093795813319607171L;

	/**
	 * 
	 */
	public YuSyntaxError() {
	}

	/**
	 * @param message
	 */
	public YuSyntaxError(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public YuSyntaxError(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public YuSyntaxError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public YuSyntaxError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
