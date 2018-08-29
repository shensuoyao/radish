package org.sam.shen.scheduing.vo;

import java.util.List;

import org.sam.shen.scheduing.entity.Agent;
import org.sam.shen.scheduing.entity.AgentHandler;

/**
 *  Agent 客户端编辑View Object
 * @author suoyao
 * @date 2018年8月10日 上午11:17:21
  * 
 */
public class AgentEditVo {

	private Agent agent;
	
	private List<AgentHandler> handlers;
	
	public AgentEditVo() {
		super();
	}
	
	public AgentEditVo(Agent agent, List<AgentHandler> handlers) {
		this();
		this.agent = agent;
		this.handlers = handlers;
	}

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public List<AgentHandler> getHandlers() {
		return handlers;
	}

	public void setHandlers(List<AgentHandler> handlers) {
		this.handlers = handlers;
	}
	
}
