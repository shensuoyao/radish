package org.sam.shen.scheduing.entity;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Getter;
import lombok.Setter;

/**
  *  客户端机器
 * @author suoyao
 * @date 2018年8月8日 下午4:00:56
  * 
 */
@Getter
@Setter
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

	// 与Agent交互的网络通信模式
	private String network;

	// netty监听的端口
	private Integer nettyPort;
	
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
	
	@Override
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteNullStringAsEmpty);
	}
	
}
