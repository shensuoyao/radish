package org.sam.shen.core.event;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import org.sam.shen.core.constants.DistributionType;
import org.sam.shen.core.constants.HandlerType;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.sam.shen.core.model.Resp;

/**
 * @author suoyao Job execution parameters
 */
@Getter
@Setter
public class HandlerEvent implements Serializable {
	private static final long serialVersionUID = 7917928400746337287L;
	
	// 执行调用ID
	private String eventId;

	// 执行的事件组ID
	private String groupId;
	
	// 配置的Job任务ID
	private String jobId;

	private String registryHandler;

	private String cmd;
	
	private HandlerType handlerType;

	private String[] params;

	private String handlerLogPath;

	private Resp<String> handlerResult;

	private DistributionType distType;

	private String eventRule;
	
	public HandlerEvent() {
		super();
	}

	public HandlerEvent(String eventId, String jobId, String registryHandler, String cmd, HandlerType handlerType) {
		this();
		this.eventId = eventId;
		this.jobId = jobId;
		this.registryHandler = registryHandler;
		this.cmd = cmd;
		this.handlerType = handlerType;
	}
	
	@Override
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteNullStringAsEmpty,
		        SerializerFeature.WriteNullListAsEmpty);
	}

}
