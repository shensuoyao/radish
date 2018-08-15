package org.sam.shen.scheduing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.sam.shen.scheduing.entity.AgentGroupRef;

@Mapper
public interface AgentGroupRefMapper {

	List<AgentGroupRef> queryAgentGroupRefByAgentGroup(Long agentGroupId);
	
	void saveAgentGroupRefBatch(List<AgentGroupRef> agentGroupRefList);
	
	void deleteAgentGroupRef(Long agentGroupId);
}
