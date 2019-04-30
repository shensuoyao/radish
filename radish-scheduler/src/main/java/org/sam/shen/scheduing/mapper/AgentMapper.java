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
	
	Page<Agent> queryAgentForPagerUser(@Param("agentName") String agentName, @Param("userId") Long userId);

	Page<Agent> queryAgentForPager(@Param("agentName") String agentName);
	
	List<Agent> queryAgentForList(@Param("agentName") String agentName);
	
	void saveAgent(Agent agent);
	
	void upgradeAgentAdmin(Agent agent);
	
	void upgradeAgent(Agent agent);
	
	Integer countAgent(@Param("userId") Long userId);
	
	List<Agent> queryAgentByAgentGroup(Long agentGroupId);
	
	List<Agent> queryAgentInIds(List<Long> ids);

	int deleteAgent(Long agentId);
	
}
