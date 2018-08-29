package org.sam.shen.scheduing.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 *  客户端注册的Handler
 * @author suoyao
 * @date 2018年8月9日 下午2:46:08
  * 
 */
public class AgentHandler extends Identity {
	private static final long serialVersionUID = 7267534292303749274L;

	private Long agentId;
	
	private String handler;
	
	private String description;
	
	private int enable = 1;
	
	public AgentHandler() {
		super();
	}
	
	public AgentHandler(Long agentId, String handler, String description) {
		this.agentId = agentId;
		this.handler = handler;
		this.description = description;
	}

	public Long getAgentId() {
		return agentId;
	}

	public void setAgentId(Long agentId) {
		this.agentId = agentId;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getEnable() {
		return enable;
	}

	public void setEnable(int enable) {
		this.enable = enable;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteNullStringAsEmpty);
	}
	
}
