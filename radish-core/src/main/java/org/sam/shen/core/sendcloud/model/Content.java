package org.sam.shen.core.sendcloud.model;


import org.sam.shen.core.sendcloud.exception.ContentException;

public interface Content {
	public boolean useTemplate();

	public boolean validate() throws ContentException;
}