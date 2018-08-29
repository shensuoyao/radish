package org.sam.shen.scheduing.entity;

import java.io.Serializable;
import java.util.Date;

import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.constants.EventStatus;
import org.sam.shen.core.constants.HandlerType;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class JobEvent implements Serializable {
	private static final long serialVersionUID = -3205360376193569666L;
	
	private String eventId;
	
	private Long jobId;
	
	private String parentJobId;
	
	private Long agentId;
	
	private String registryHandler;
	
	private HandlerType handlerType;    // job handler 处理器 类型
	
	private String cmd;    // 执行命令
	
	private String params;    // 附加参数
	
	private EventStatus stat;
	
	private int retryCount;
	
	private Date createTime;
	
	public JobEvent() {
		super();
		this.createTime = new Date();
	}
	
	public JobEvent(Long jobId, Long agentId, HandlerType handlerType, EventStatus stat) {
		this();
		this.eventId = jobId + Constant.SPLIT_CHARACTER + System.currentTimeMillis();
		this.jobId = jobId;
		this.agentId = agentId;
		this.handlerType = handlerType;
		this.stat = stat;
	}
	
	public JobEvent(Long jobId, Long agentId, HandlerType handlerType, EventStatus stat, String cmd, String params) {
		this(jobId, agentId, handlerType, stat);
		this.cmd = cmd;
		this.params = params;
	}
	
	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public String getParentJobId() {
		return parentJobId;
	}

	public void setParentJobId(String parentJobId) {
		this.parentJobId = parentJobId;
	}

	public Long getAgentId() {
		return agentId;
	}

	public void setAgentId(Long agentId) {
		this.agentId = agentId;
	}

	public String getRegistryHandler() {
		return registryHandler;
	}

	public void setRegistryHandler(String registryHandler) {
		this.registryHandler = registryHandler;
	}

	public HandlerType getHandlerType() {
		return handlerType;
	}

	public void setHandlerType(HandlerType handlerType) {
		this.handlerType = handlerType;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public EventStatus getStat() {
		return stat;
	}

	public void setStat(EventStatus stat) {
		this.stat = stat;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteMapNullValue,
		        SerializerFeature.WriteNullListAsEmpty);
	}
	
}
