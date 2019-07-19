package org.radish.spring.boot.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.agent.RadishAgent;
import org.sam.shen.core.handler.IHandler;
import org.sam.shen.core.handler.anno.AHandler;
import org.sam.shen.core.handler.impl.ScriptHandler;
import org.sam.shen.core.model.AgentInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author clock
 * @date 2018/12/19 上午9:48
 */
@Slf4j
@Configuration
@ConditionalOnClass(RadishAgent.class)
@EnableConfigurationProperties(RadishProperties.class)
public class RadishAutoConfiguration {

    @Value("${server.port}")
    private int serverPort;

    private final RadishProperties properties;

    public RadishAutoConfiguration(RadishProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(ScriptHandler.class)
    public ScriptHandler scriptHandler() {
        return new ScriptHandler();
    }

    @Bean(initMethod = "start", destroyMethod = "destroy")
    @ConditionalOnMissingBean(RadishAgent.class)
    public RadishAgent radishAgent(ApplicationContext applicationContext) {
        log.info(">>>>>>>>>>> radish-agent config init.");

        // Set event handlers
        if(null == applicationContext) {
            log.error("ApplicationContext is not ready.");
            throw new RuntimeException("ApplicationContext is not ready.");
        }
        List<IHandler> handlers = new ArrayList<>();
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(AHandler.class);
        for (Object bean : serviceBeanMap.values()) {
            if (bean instanceof IHandler) {
                handlers.add((IHandler) bean);
            }
        }
        RadishAgent radishAgent = new RadishAgent(handlers);

        // Set scheduler information
        RadishProperties.Scheduler scheduler = this.properties.getScheduler();
        if (scheduler == null || StringUtils.isEmpty(scheduler.getServer())) {
            log.error("Scheduler server can't be null.");
            throw new RuntimeException("Scheduler server can't be null.");
        }
        radishAgent.setScheduingServer(scheduler.getServer());

        // set agent information
        RadishProperties.Agent agent = this.properties.getAgent();
        AgentInfo agentInfo = radishAgent.getAgentInfo();
        if (agent != null) {
            String agentName = agent.getName();
            String agentIp = agent.getIp();
            Integer agentPort = agent.getPort();
            String logPath = agent.getLogpath();
            String shPath = agent.getShpath();
            if (StringUtils.isNotEmpty(logPath)) {
                radishAgent.setLogPath(logPath);
            }
            if (StringUtils.isNotEmpty(shPath)) {
                radishAgent.setShPath(shPath);
            }
            if (StringUtils.isNotEmpty(agentIp)) {
                agentInfo.setAgentIp(agentIp);
            }
            if (StringUtils.isNotEmpty(agentName)) {
                agentInfo.setAgentName(agentName);
            }
            if (agentPort != null) {
                agentInfo.setAgentPort(agentPort);
            } else {
                agentInfo.setAgentPort(serverPort);
            }
        }
        // set network
        String network = this.properties.getLogViewMode();
        RadishProperties.LogViewNetty nettyProperties = this.properties.getLogViewNetty();
        agentInfo.setNetwork(network);
        if (nettyProperties != null && nettyProperties.getPort() != null) {
            agentInfo.setNettyPort(nettyProperties.getPort());
        }
        // set time interval
        if (properties.getHeartBeat() != null) {
            radishAgent.setHeartBeat(properties.getHeartBeat());
        }
        if (properties.getTriggerBeat() != null) {
            radishAgent.setTriggerBeat(properties.getTriggerBeat());
        }
        if (properties.getHandleEventBeat() != null) {
            radishAgent.setHandleEventBeat(properties.getHandleEventBeat());
        }
        // set core pool size
        if (properties.getCorePoolSize() != null) {
            radishAgent.setCorePoolSize(properties.getCorePoolSize());
        }
        return radishAgent;
    }

}
