package org.radish.spring.boot.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.agent.RadishAgent;
import org.sam.shen.core.handler.IHandler;
import org.sam.shen.core.handler.anno.AHandler;
import org.sam.shen.core.handler.impl.ScriptHandler;
import org.sam.shen.core.model.AgentInfo;
import org.sam.shen.core.util.IpUtil;
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

        // Set basic agent information
        String server = this.properties.getScheduler().getServer();
        String agentName = this.properties.getAgent().getName();
        Integer agentPort = this.properties.getAgent().getPort();
        String network = this.properties.getLogViewMode();
        RadishProperties.LogViewNetty nettyProperties = this.properties.getLogViewNetty();
        if (StringUtils.isEmpty(server)) {
            log.error("Scheduler server can't be null.");
            throw new RuntimeException("Scheduler server can't be null.");
        }
        radishAgent.setScheduingServer(server);
        radishAgent.setLogPath(properties.getAgent().getLogpath());
        radishAgent.setShPath(properties.getAgent().getShpath());
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setAgentIp(properties.getAgent().getIp());
        if (StringUtils.isEmpty(agentName)) {
            agentName = IpUtil.getHostName();
        }
        agentInfo.setAgentName(agentName);
        agentInfo.setAgentPort(agentPort);
        agentInfo.setNetwork(network);
        if (nettyProperties != null) {
            agentInfo.setNettyPort(nettyProperties.getPort());
        }
        radishAgent.setAgentInfo(agentInfo);

        return radishAgent;
    }
}
