package org.sam.shen.scheduing.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
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

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTime;
	
	private Integer priority;
	
	private Integer enable;

	private Long userId; // 创建的用户ID

	private String expired; // 事件过期时间

	public JobInfo() {
		super();
	}
	
	@Override
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteMapNullValue,
		        SerializerFeature.WriteNullListAsEmpty);
	}
	
}
