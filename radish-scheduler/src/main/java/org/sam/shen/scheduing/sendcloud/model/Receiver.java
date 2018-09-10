package org.sam.shen.scheduing.sendcloud.model;

import org.sam.shen.scheduing.sendcloud.exception.ReceiverException;

public interface Receiver {
	public boolean useAddressList();
	
	public boolean validate() throws ReceiverException;
	
	public String toString();
}