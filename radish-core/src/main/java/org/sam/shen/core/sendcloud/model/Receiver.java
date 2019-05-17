package org.sam.shen.core.sendcloud.model;


import org.sam.shen.core.sendcloud.exception.ReceiverException;

public interface Receiver {
	public boolean useAddressList();
	
	public boolean validate() throws ReceiverException;
	
	public String toString();
}