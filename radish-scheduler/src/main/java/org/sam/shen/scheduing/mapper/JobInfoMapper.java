package org.sam.shen.scheduing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sam.shen.scheduing.entity.JobInfo;

import com.github.pagehelper.Page;

@Mapper
public interface JobInfoMapper {

	void saveJobInfo(JobInfo jobInfo);
	
	JobInfo findJobInfoById(Long id);
	
	Page<JobInfo> queryJobInfoForPager(@Param("jobName") String jobName);
	
	List<JobInfo> queryJobInfoForList(@Param("jobName") String jobName);
	
	List<JobInfo> queryJobInfoInIds(List<Long> ids);
	
	void upgradeJonInfo(JobInfo jobInfo);
	
	List<JobInfo> queryJobInfoByEnable(int enable);
	
	// ---------------  统计  ------------------------
	
	Integer countJobInfoByEnable(int enable);

	List<JobInfo> findJobInfoByParentId(String id);

    /**
     * 用于查询启动时待加载的job
     * @return 待加载的job
     */
	List<JobInfo> queryLoadedJobs();
	
}
