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
	
	// Agent 注册的handler处理器 多个用 英文半角 逗号(,)隔开 
	private String regHandler;
	
	// Agent 管理员
	private String admin;
	
	// Agent 管理员邮箱
	private String adminEmail;
	
	// Agent 管理员电话号码
	private String adminPhone;
	
	// Agent注册时间
	private Date regTime;
	
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
	public String getRegHandler() {
		return regHandler;
	}
	public void setRegHandler(String regHandler) {
		this.regHandler = regHandler;
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
	@Override
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteNullStringAsEmpty);
	}
	
}
