package org.sam.shen.scheduing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.sam.shen.scheduing.entity.AgentGroup;

@Mapper
public interface AgentGroupMapper {
	
	AgentGroup findAgentGroupById(Long id);

	void saveAgentGroup(AgentGroup agentGroup);
	
	List<AgentGroup> queryAgentGroup();
	
	Long countAgentGroup();
	
	void upgradeAgentGroup(AgentGroup agentGroup);
}
