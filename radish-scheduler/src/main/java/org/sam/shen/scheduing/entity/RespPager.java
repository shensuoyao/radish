package org.sam.shen.scheduing.entity;

import org.sam.shen.core.model.Resp;

public class RespPager<T> extends Resp<T> {
	private static final long serialVersionUID = 5933017493760651628L;

	private int limit;
	
	private long count;
	
	public RespPager(T t) {
		super(t);
	}
	
	public RespPager(int limit, long count, T t) {
		super(t);
		this.limit = limit;
		this.count = count;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
	
}
