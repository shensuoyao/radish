package org.sam.shen.scheduing.vo;

import java.util.Date;

public class SchedulerJobVo {

	private String jobId;
	
	private String jobName;
	
	private String crontab;
	
	private Date prevFireTime;
	
	private Date nextFireTime;
	
	public SchedulerJobVo() {
		super();
	}
	
	public SchedulerJobVo(String jobId, String jobName) {
		this.jobId = jobId;
		this.jobName = jobName;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
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

	public Date getPrevFireTime() {
		return prevFireTime;
	}

	public void setPrevFireTime(Date prevFireTime) {
		this.prevFireTime = prevFireTime;
	}

	public Date getNextFireTime() {
		return nextFireTime;
	}

	public void setNextFireTime(Date nextFireTime) {
		this.nextFireTime = nextFireTime;
	}
	
}
