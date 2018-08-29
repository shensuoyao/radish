package org.sam.shen.scheduing;

import java.util.List;

import javax.annotation.Resource;

import org.quartz.SchedulerException;
import org.sam.shen.scheduing.entity.JobInfo;
import org.sam.shen.scheduing.mapper.JobInfoMapper;
import org.sam.shen.scheduing.scheduler.RadishDynamicScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author suoyao
 * @date 2018年8月6日 上午8:39:59
 *  Radish Scheduing Start Application
 */
@SpringBootApplication
public class RadishScheduingApplication implements ApplicationRunner {
	
	private final Logger logger = LoggerFactory.getLogger(RadishScheduingApplication.class);

	@Resource
	private JobInfoMapper jobInfoMappper;
	
	public static void main(String[] args) {
		SpringApplication.run(RadishScheduingApplication.class, args);
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.boot.ApplicationRunner#run(org.springframework.boot.ApplicationArguments)
	 */
	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (args.containsOption("loadjob")) {
			// 从数据库加载jobinfo生成任务集合
			List<JobInfo> enableJobInfo = jobInfoMappper.queryJobInfoByEnable(1);
			if (null != enableJobInfo && enableJobInfo.size() > 0) {
				enableJobInfo.forEach(jobInfo -> {
					try {
						RadishDynamicScheduler.addJob(jobInfo.getId(), jobInfo.getJobName(), jobInfo.getCrontab());
					} catch (SchedulerException e) {
						logger.error("init add jobInfo failed. {}", jobInfo.getJobName());
					}
				});
			}
		}
	}
}
