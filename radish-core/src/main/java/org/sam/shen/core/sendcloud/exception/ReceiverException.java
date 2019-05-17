package org.sam.shen.core.sendcloud.exception;

public class ReceiverException extends Throwable implements SCException {

	private static final long serialVersionUID = 5740175732854853130L;
	private static final int errorCode = 301;

	public ReceiverException(String message) {
		super(message);
	}

	public String getMessage() {
		return String.format("code:%d,message:%s", errorCode, super.getMessage());
	}

}