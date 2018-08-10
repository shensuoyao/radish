package org.sam.shen.scheduing.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sam.shen.scheduing.entity.Agent;

import com.github.pagehelper.Page;

@Mapper
public interface AgentMapper {

	Agent findAgentByName(String agentName);
	
	Agent findAgentById(Long id);
	
	Page<Agent> queryAgentForPager(@Param("agentName") String agentName);
	
	void saveAgent(Agent agent);
	
}
