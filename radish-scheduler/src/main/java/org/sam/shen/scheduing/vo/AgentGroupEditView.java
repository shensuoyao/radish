package org.sam.shen.scheduing.vo;

import java.util.List;

import org.sam.shen.scheduing.entity.Agent;
import org.sam.shen.scheduing.entity.AgentGroup;

public class AgentGroupEditView {

	private AgentGroup agentGroup;
	
	private List<Agent> agents;
	
	public AgentGroupEditView() {
		super();
	}
	
	public AgentGroupEditView(AgentGroup agentGroup, List<Agent> agents) {
		this();
		this.agentGroup = agentGroup;
		this.agents = agents;
	}

	public AgentGroup getAgentGroup() {
		return agentGroup;
	}

	public void setAgentGroup(AgentGroup agentGroup) {
		this.agentGroup = agentGroup;
	}

	public List<Agent> getAgents() {
		return agents;
	}

	public void setAgents(List<Agent> agents) {
		this.agents = agents;
	}
	
}
