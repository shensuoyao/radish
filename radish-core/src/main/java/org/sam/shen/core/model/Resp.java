package org.sam.shen.core.model;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

/**
 * @author suoyao
 * @param <T>
 * Object generic returned by the service call
 */
public class Resp<T> implements Serializable {
	private static final long serialVersionUID = 6852556469469562131L;
	
	// 执行器处理成功
	public static final Resp<String> SUCCESS = new Resp<String>();

	// 执行器处理失败
	public static final Resp<String> FAIL = new Resp<String>(1, "failed");

	/**
	 *  returned code
	 *   0 is successed and other is wrong
	 */
	private int code;

	/**
	 *  returned message
	 */
	private String msg = "success";

	/**
	 *  returned object body
	 */
	private T data;

	public Resp() {
		super();
	}
	
	public Resp(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public Resp(int code, String msg, T t) {
		this(msg, t);
		this.code = code;
	}
	
	public Resp(String msg, T t) {
		this.msg = msg;
		this.data = t;
	}
	
	public Resp(T t) {
		this.data = t;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	
	public String toJsonData() {
		return JSON.toJSONString(data);
	}
	
}
