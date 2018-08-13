package org.sam.shen.scheduing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.sam.shen.scheduing.entity.AgentGroup;

@Mapper
public interface AgentGroupMapper {

	void saveAgentGroup(AgentGroup agentGroup);
	
	List<AgentGroup> queryAgentGroup();
}
