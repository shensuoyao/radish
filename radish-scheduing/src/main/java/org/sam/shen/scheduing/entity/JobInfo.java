package org.sam.shen.scheduing.entity;

import java.util.Date;

import org.sam.shen.core.constants.HandlerFailStrategy;
import org.sam.shen.core.constants.HandlerType;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class JobInfo extends Identity {
	private static final long serialVersionUID = -3205360376193569666L;
	
	// id: quartz name
	
	private String jobName;    // quartz group is jobName.hashCode()
	
	private String crontab;	// quartz 表达式
	
	private HandlerType jobType;    // job 类型
	
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
	
	private String parentJobId;    // 父任务ID
	
	private String childJobId;    // 子任务Id
	
	private Date createTime;
	
	private Date updateTime;
	
	private int enable;
	
	public JobInfo() {
		super();
	}
	
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getCrontab() {
		return crontab;
	}

	public void setCrontab(String crontab) {
		this.crontab = crontab;
	}

	public HandlerType getJobType() {
		return jobType;
	}

	public void setJobType(HandlerType jobType) {
		this.jobType = jobType;
	}

	public HandlerFailStrategy getHandlerFailStrategy() {
		return handlerFailStrategy;
	}

	public void setHandlerFailStrategy(HandlerFailStrategy handlerFailStrategy) {
		this.handlerFailStrategy = handlerFailStrategy;
	}

	public String getAdmin() {
		return admin;
	}

	public void setAdmin(String admin) {
		this.admin = admin;
	}

	public String getAdminPhone() {
		return adminPhone;
	}

	public void setAdminPhone(String adminPhone) {
		this.adminPhone = adminPhone;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	public String getExecutorHandlers() {
		return executorHandlers;
	}

	public void setExecutorHandlers(String executorHandlers) {
		this.executorHandlers = executorHandlers;
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

	public String getChildJobId() {
		return childJobId;
	}

	public void setChildJobId(String childJobId) {
		this.childJobId = childJobId;
	}

	public String getParentJobId() {
		return parentJobId;
	}

	public void setParentJobId(String parentJobId) {
		this.parentJobId = parentJobId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public int getEnable() {
		return enable;
	}

	public void setEnable(int enable) {
		this.enable = enable;
	}
	
	@Override
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteMapNullValue,
		        SerializerFeature.WriteNullListAsEmpty);
	}
	
}
