package org.sam.shen.scheduing.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sam.shen.scheduing.entity.JobInfo;

import com.github.pagehelper.Page;

@Mapper
public interface JobInfoMapper {

	void saveJobInfo(JobInfo jobInfo);
	
	JobInfo findJobInfoById(Long id);
	
	Page<JobInfo> queryJobInfoForPager(@Param("jobName") String jobName);
	
}
