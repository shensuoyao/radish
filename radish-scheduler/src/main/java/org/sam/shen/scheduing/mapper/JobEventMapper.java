package org.sam.shen.scheduing.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.sam.shen.scheduing.entity.JobEvent;

@Mapper
public interface JobEventMapper {

	void saveJobEventBatch(List<JobEvent> events);
	
	List<JobEvent> queryJobInfoByAgentId(Long agentId);
	
	Integer countJobEventInJobIds(List<Long> ids);
	
	void deleteJobEventNotEqual(Map<String, Object> param);
	
}
