package org.sam.shen.scheduing.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
  *  客户端与客户端组映射关系
 * @author suoyao
 * @date 2018年8月8日 下午4:01:13
  * 
 */
public class AgentGroupRef extends Identity {
	private static final long serialVersionUID = 1551264174238873923L;

	// Agent ID
	private Long agentId;
	
	// Agent Group ID
	private Long agentGroupId;
	
	public AgentGroupRef() {
		super();
	}
	
	public AgentGroupRef(Long agentId, Long agentGroupId) {
		this.agentId = agentId;
		this.agentGroupId = agentGroupId;
	}
	
	public Long getAgentId() {
		return agentId;
	}

	public void setAgentId(Long agentId) {
		this.agentId = agentId;
	}

	public Long getAgentGroupId() {
		return agentGroupId;
	}

	public void setAgentGroupId(Long agentGroupId) {
		this.agentGroupId = agentGroupId;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteNullStringAsEmpty);
	}
}
