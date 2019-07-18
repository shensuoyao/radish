package org.sam.shen.scheduing.entity;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.constants.DistributionType;
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

	private String groupId;

	private String parentGroupId;
	
	/**
	 * 执行任务的Handler处理器
	 * 格式: agentId-handler[, agentId-handler ...]
	 */
	private String executorHandlers;
	
	private HandlerType handlerType;    // job handler 处理器 类型
	
	private String cmd;    // 执行命令
	
	private String params;    // 附加参数

    private String paramFilePath; // 附加参数附件路径
	
	private EventStatus stat;
	
	private Long handlerAgentId;
	
	private int priority;		// 优先级 从 0 - 9
	
	private int retryCount;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date triggerTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date handleTime;

	private String handlerLogPath;

	private DistributionType distType;

	private String eventRule;
	
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

	public JobEvent(Long jobId, Long time, int serial, String groupId, String executorHandlers, HandlerType handlerType,
                    EventStatus stat, int priority, String cmd, String params, DistributionType distType, String eventRule) {
	    this(jobId, executorHandlers, handlerType, stat, priority, cmd, params);
	    // 重新设置eventId
        this.eventId = jobId + Constant.SPLIT_CHARACTER + time + Constant.SPLIT_CHARACTER + serial;
        this.groupId = groupId;
        this.distType = distType;
        this.eventRule = eventRule;
    }

	@Override
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteMapNullValue,
		        SerializerFeature.WriteNullListAsEmpty);
	}
	
}
