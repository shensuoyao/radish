package org.sam.shen.scheduing.entity;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
  *  客户端机器
 * @author suoyao
 * @date 2018年8月8日 下午4:00:56
  * 
 */
public class Agent extends Identity {
	private static final long serialVersionUID = 4990478412611560873L;

	// Agent 名字
	private String agentName;
	
	// Agent IP
	private String agentIp;
	
	//Agent 端口号
	private Integer agentPort;
	
	// Agent 管理员
	private String admin;
	
	// Agent 管理员邮箱
	private String adminEmail;
	
	// Agent 管理员电话号码
	private String adminPhone;
	
	// Agent注册时间
	private Date regTime;
	
	// 状态
	private int stat;
	
	public Agent() {
		super();
	}
	
	public Agent(String agentName, String agentIp, Integer agentPort) {
		this.agentName = agentName;
		this.agentIp = agentIp;
		this.agentPort = agentPort;
		this.regTime = new Date();
		this.stat = 1;
	}
	
	public String getAgentName() {
		return agentName;
	}
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	public String getAgentIp() {
		return agentIp;
	}
	public void setAgentIp(String agentIp) {
		this.agentIp = agentIp;
	}
	public Integer getAgentPort() {
		return agentPort;
	}
	public void setAgentPort(Integer agentPort) {
		this.agentPort = agentPort;
	}
	public String getAdmin() {
		return admin;
	}
	public void setAdmin(String admin) {
		this.admin = admin;
	}
	public String getAdminEmail() {
		return adminEmail;
	}
	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}
	public String getAdminPhone() {
		return adminPhone;
	}
	public void setAdminPhone(String adminPhone) {
		this.adminPhone = adminPhone;
	}
	public Date getRegTime() {
		return regTime;
	}
	public void setRegTime(Date regTime) {
		this.regTime = regTime;
	}
	
	public int getStat() {
		return stat;
	}

	public void setStat(int stat) {
		this.stat = stat;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteNullStringAsEmpty);
	}
	
}
