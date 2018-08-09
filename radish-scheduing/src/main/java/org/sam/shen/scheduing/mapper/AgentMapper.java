package org.sam.shen.scheduing.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.sam.shen.scheduing.entity.Agent;

import com.github.pagehelper.Page;

@Mapper
public interface AgentMapper {

	Agent findAgentByName(String agentName);
	
	Page<Agent> queryAgentForPager();
	
	void saveAgent(Agent agent);
	
}
