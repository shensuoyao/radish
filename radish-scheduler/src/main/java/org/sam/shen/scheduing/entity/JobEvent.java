package org.sam.shen.scheduing.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.constants.EventStatus;
import org.sam.shen.core.constants.HandlerType;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

@Getter
@Setter
public class JobEvent implements Serializable {
	private static final long serialVersionUID = -3205360376193569666L;
	
	private String eventId;

	private String parentEventId;
	
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

	private String handlerLogPath;
	
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

	@Override
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteMapNullValue,
		        SerializerFeature.WriteNullListAsEmpty);
	}
	
}
