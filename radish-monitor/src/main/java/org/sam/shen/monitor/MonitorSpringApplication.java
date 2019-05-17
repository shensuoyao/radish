package org.sam.shen.monitor;

import org.sam.shen.monitor.thread.AlarmCenter;
import org.sam.shen.monitor.thread.MonitoringCenter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author clock
 * @date 2019-05-13 18:03
 */
@Configuration
@SpringBootApplication
public class MonitorSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorSpringApplication.class, args);
    }

    @Bean
    public MonitoringCenter monitoringCenter() {
        return new MonitoringCenter(alarmCenter());
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    public AlarmCenter alarmCenter() {
        return new AlarmCenter();
    }

}
