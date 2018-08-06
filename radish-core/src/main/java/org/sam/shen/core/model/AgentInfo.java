package org.sam.shen.core.model;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class AgentInfo {

	private String agentName;
	
	private String agentIp;
	
	private int agentPort;
	
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
