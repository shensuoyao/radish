package org.sam.shen.scheduing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.sam.shen.scheduing.entity.JobEvent;

@Mapper
public interface JobEventMapper {

	void saveJobEventBatch(List<JobEvent> events);
	
}
