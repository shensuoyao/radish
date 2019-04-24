package org.sam.shen.scheduing;

import java.util.List;

import javax.annotation.Resource;

import org.quartz.SchedulerException;
import org.sam.shen.scheduing.cluster.ClusterPeer;
import org.sam.shen.scheduing.cluster.ClusterPeerConfig;
import org.sam.shen.scheduing.entity.JobInfo;
import org.sam.shen.scheduing.mapper.JobInfoMapper;
import org.sam.shen.scheduing.scheduler.RadishDynamicScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author suoyao
 * @date 2018年8月6日 上午8:39:59
 *  Radish Scheduing Start Application
 */
@Configuration
@EnableRedisHttpSession
@SpringBootApplication
public class RadishScheduingApplication implements ApplicationRunner {
	
	private final Logger logger = LoggerFactory.getLogger(RadishScheduingApplication.class);

	@Resource
	private JobInfoMapper jobInfoMappper;
	
	@Resource
	private ClusterPeer clusterPeer;
	
	@Resource
	private ClusterPeerConfig clusterPeerConfig;
	
	public static void main(String[] args) {
		SpringApplication.run(RadishScheduingApplication.class, args);
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.boot.ApplicationRunner#run(org.springframework.boot.ApplicationArguments)
	 */
	@Override
	public void run(ApplicationArguments args) throws Exception {
		// 加载调度任务
		if (args.containsOption("loadjob")) {
			// 从数据库加载jobinfo生成任务集合
			List<JobInfo> enableJobInfo = jobInfoMappper.queryLoadedJobs();
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
		
		if(clusterPeerConfig.isClusterDeploy()) {
			// 初始化配置
			clusterPeerConfig = clusterPeerConfig.init();
			// 启动集群服务
			clusterPeer.setMyId(clusterPeerConfig.getNid());
			clusterPeer.setInitLimit(clusterPeerConfig.getInitLimit());
			clusterPeer.setTickTime(clusterPeerConfig.getTickTime());
			clusterPeer.setSyncLimit(clusterPeerConfig.getSyncLimit());
			clusterPeer.setCnxTimeout(clusterPeerConfig.getCnxTimeout());
			clusterPeer.setClusterListenOnAllIPs(clusterPeerConfig.isClusterListenOnAllIPs());
			clusterPeer.setClusterServers(clusterPeerConfig.getClusterPeerServers());
			clusterPeer.start();
		}
		
	}

	@Bean
    public ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }
	
}
