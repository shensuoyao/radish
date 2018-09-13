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
	
	/**
	 * 执行任务的Handler处理器
	 * 格式: agentId-handler[, agentId-handler ...]
	 */
	private String executorHandlers;
	
	private HandlerType handlerType;    // job handler 处理器 类型
	
	private String cmd;    // 执行命令
	
	private String params;    // 附加参数
	
	private EventStatus stat;
	
	private Long handlerAgentId;
	
	private int priority;		// 优先级 从 0 - 9
	
	private int retryCount;
	
	private Date createTime;
	
	public JobEvent() {
		super();
		this.createTime = new Date();
	}
	
	public JobEvent(Long jobId, String executorHandlers, HandlerType handlerType, EventStatus stat, int priority) {
		this();
		this.eventId = jobId + Constant.SPLIT_CHARACTER + System.currentTimeMillis();
		this.jobId = jobId;
		this.executorHandlers = executorHandlers;
		this.handlerType = handlerType;
		this.stat = stat;
		this.priority = priority;
	}
	
	public JobEvent(Long jobId, String executorHandlers, HandlerType handlerType, EventStatus stat, int priority, String cmd,
	        String params) {
		this(jobId, executorHandlers, handlerType, stat, priority);
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

	public String getExecutorHandlers() {
		return executorHandlers;
	}

	public void setExecutorHandlers(String executorHandlers) {
		this.executorHandlers = executorHandlers;
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

	public Long getHandlerAgentId() {
		return handlerAgentId;
	}

	public void setHandlerAgentId(Long handlerAgentId) {
		this.handlerAgentId = handlerAgentId;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
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
