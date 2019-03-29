package org.sam.shen.scheduing.cluster;

import lombok.Getter;
import lombok.Setter;

/**
 * leader发送的同步信息
 * @author suoyao
 * @date 2019年3月15日 下午5:28:25
  * 
 */
@Getter
@Setter
public class LeaderInfo {
	
	private Long jobId;
	
	private String jobName;
	
	private String crontab;
	
    public LeaderInfo() {}

    public LeaderInfo(Long jobId, String jobName, String crontab) {
		this.jobId = jobId;
		this.jobName = jobName;
		this.crontab = crontab;
	}

}
