package org.sam.shen.scheduing.sendcloud.exception;

public class ReceiverException extends Throwable implements SCException {

	private static final long serialVersionUID = 1L;
	private static final int errorCode = 301;

	public ReceiverException(String message) {
		super(message);
	}

	public String getMessage() {
		return String.format("code:%d,message:%s", errorCode, super.getMessage());
	}

}