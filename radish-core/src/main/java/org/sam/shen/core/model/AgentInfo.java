package org.sam.shen.core.model;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 *  Agent客户端信息
 * @author suoyao
 * @date 2018年8月7日 上午9:16:07
  *  
 */
public class AgentInfo {

	// Agent 管理名(需要唯一)
	private String agentName;
	
	// ip
	private String agentIp;
	
	// Rest RPC 端口
	private int agentPort;
	
	// Agent中注册的Handler处理器集合
	private Map<String, String> registryHandlerMap;

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

	public int getAgentPort() {
		return agentPort;
	}

	public void setAgentPort(int agentPort) {
		this.agentPort = agentPort;
	}

	public Map<String, String> getRegistryHandlerMap() {
		return registryHandlerMap;
	}

	public void setRegistryHandlerMap(Map<String, String> registryHandlerMap) {
		this.registryHandlerMap = registryHandlerMap;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteMapNullValue);
	}
	
}
