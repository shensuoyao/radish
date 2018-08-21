package org.sam.shen.scheduing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sam.shen.scheduing.entity.Agent;

import com.github.pagehelper.Page;

@Mapper
public interface AgentMapper {

	Agent findAgentByName(String agentName);
	
	Agent findAgentById(Long id);
	
	Page<Agent> queryAgentForPager(@Param("agentName") String agentName);
	
	List<Agent> queryAgentForList(@Param("agentName") String agentName);
	
	void saveAgent(Agent agent);
	
	void upgradeAgentAdmin(Agent agent);
	
	void upgradeAgent(Agent agent);
	
	Long countAgent(int stat);
	
	List<Agent> queryAgentByAgentGroup(Long agentGroupId);
	
	List<Agent> queryAgentInIds(List<Long> ids);
	
}
