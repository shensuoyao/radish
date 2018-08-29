package org.sam.shen.core.event;

import java.io.Serializable;

import org.sam.shen.core.constants.HandlerType;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author suoyao Job execution parameters
 */
public class HandlerEvent implements Serializable {
	private static final long serialVersionUID = 7917928400746337287L;
	
	// 执行调用ID
	private String eventId;
	
	// 配置的Job任务ID
	private String jobId;

	private String registryHandler;

	private String cmd;
	
	private HandlerType handlerType;

	private String[] params;
	
	public HandlerEvent() {
		super();
	}

	public HandlerEvent(String eventId, String jobId, String registryHandler, String cmd, HandlerType handlerType,
	        String[] params) {
		this();
		this.eventId = eventId;
		this.jobId = jobId;
		this.registryHandler = registryHandler;
		this.cmd = cmd;
		this.handlerType = handlerType;
		this.params = params;
	}
	
	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getRegistryHandler() {
		return registryHandler;
	}

	public void setRegistryHandler(String registryHandler) {
		this.registryHandler = registryHandler;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public HandlerType getHandlerType() {
		return handlerType;
	}

	public void setHandlerType(HandlerType handlerType) {
		this.handlerType = handlerType;
	}

	public String[] getParams() {
		return params;
	}

	public void setParams(String[] params) {
		this.params = params;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteNullStringAsEmpty,
		        SerializerFeature.WriteNullListAsEmpty);
	}

}
