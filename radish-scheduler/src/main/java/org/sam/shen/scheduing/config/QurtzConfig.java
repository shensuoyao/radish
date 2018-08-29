package org.sam.shen.scheduing.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QurtzConfig {

	@Value("${quartz.path}")
	private String quartzConfigPath;
	
	@Bean
	public Properties quartzProperties() throws IOException {
		PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		propertiesFactoryBean.setLocation(new ClassPathResource(quartzConfigPath));
		propertiesFactoryBean.afterPropertiesSet();
		return propertiesFactoryBean.getObject();
	}
	
	@Bean(name = "quartzScheduler")
	public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
		SchedulerFactoryBean factory = new SchedulerFactoryBean();
		factory.setAutoStartup(true);
		factory.setStartupDelay(20);
		// 覆盖DB中JOB：true、以数据库中已经存在的为准：false
		factory.setOverwriteExistingJobs(true);
		factory.setQuartzProperties(quartzProperties());
		factory.setApplicationContextSchedulerContextKey("applicationContextKey");
		return factory;
	}
	
}
