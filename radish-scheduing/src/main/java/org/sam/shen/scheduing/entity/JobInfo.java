package org.sam.shen.scheduing.entity;

import java.util.Date;
import java.util.List;

import org.sam.shen.core.constants.HandlerTypeEnum;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class JobInfo extends Identity {
	private static final long serialVersionUID = -3205360376193569666L;
	
	// id: quartz name
	
	private String jobName;    // quartz group is jobName.hashCode()
	
	private String cron;	// quartz 表达式
	
	private HandlerTypeEnum jobType;    // job 类型
	
	private String admin;
	
	private String adminPhone;
	
	private String adminEmail;
	
	private List<String> executorHandlers;	    // 执行任务的Handler处理器
	
	private List<String> params;    // 附加参数
	
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

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public HandlerTypeEnum getJobType() {
		return jobType;
	}

	public void setJobType(HandlerTypeEnum jobType) {
		this.jobType = jobType;
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

	public List<String> getExecutorHandlers() {
		return executorHandlers;
	}

	public void setExecutorHandlers(List<String> executorHandlers) {
		this.executorHandlers = executorHandlers;
	}

	public List<String> getParams() {
		return params;
	}

	public void setParams(List<String> params) {
		this.params = params;
	}

	public String getChildJobId() {
		return childJobId;
	}

	public void setChildJobId(String childJobId) {
		this.childJobId = childJobId;
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
