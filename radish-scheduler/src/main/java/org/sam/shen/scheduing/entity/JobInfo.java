package org.sam.shen.scheduing.entity;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import org.sam.shen.core.constants.DistributionType;
import org.sam.shen.core.constants.HandlerFailStrategy;
import org.sam.shen.core.constants.HandlerType;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

@Getter
@Setter
public class JobInfo extends Identity {
	private static final long serialVersionUID = -3205360376193569666L;
	
	// id: quartz name
	
	private String jobName;    // quartz group is jobName.hashCode()
	
	private String crontab;	// quartz 表达式
	
	private HandlerType handlerType;    // job handler 处理器 类型
	
	// 任务事件处理失败策略 {丢弃 / 重试 / 告警}
	private HandlerFailStrategy handlerFailStrategy;
	
	private String admin;
	
	private String adminPhone;
	
	private String adminEmail;
	
	/**
	 * 执行任务的Handler处理器
	 * 格式: agentId-handler[, agentId-handler ...]
	 */
	private String executorHandlers;
	
	private String cmd;    // 执行命令
	
	private String params;    // 附加参数

	private String paramFilePath; // 附加参数附件路径
	
	private String parentJobId;    // 父任务ID

	private DistributionType distType; // 分片类型

    private String distRule; // 分片规则
	
	private Date createTime;
	
	private Date updateTime;
	
	private int priority;
	
	private int enable;
	
	public JobInfo() {
		super();
	}
	
	@Override
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteMapNullValue,
		        SerializerFeature.WriteNullListAsEmpty);
	}
	
}
