package org.sam.shen.scheduing.config;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerConfig {
	
	@Bean
	public Scheduler scheduler() throws SchedulerException {
		SchedulerFactory schedulerFactoryBean = new StdSchedulerFactory();
		return schedulerFactoryBean.getScheduler();
	}
	
}
