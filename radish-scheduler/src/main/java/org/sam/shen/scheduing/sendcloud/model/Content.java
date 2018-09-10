package org.sam.shen.scheduing.sendcloud.model;

import org.sam.shen.scheduing.sendcloud.exception.ContentException;

public interface Content {
	public boolean useTemplate();

	public boolean validate() throws ContentException;
}