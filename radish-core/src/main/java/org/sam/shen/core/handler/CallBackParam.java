package org.sam.shen.core.handler;

import java.io.Serializable;

import org.sam.shen.core.constants.HandlerTypeEnum;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author suoyao Job execution parameters
 */
public class CallBackParam implements Serializable {
	private static final long serialVersionUID = 7917928400746337287L;
	
	private String jobId;

	private String registryHandler;

	private String cmd;
	
	private HandlerTypeEnum handlerType;

	private String[] params;

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

	public HandlerTypeEnum getHandlerType() {
		return handlerType;
	}

	public void setHandlerType(HandlerTypeEnum handlerType) {
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
