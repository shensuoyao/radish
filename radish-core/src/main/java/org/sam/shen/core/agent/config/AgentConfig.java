package org.sam.shen.core.agent.config;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.agent.RadishAgent;
import org.sam.shen.core.model.AgentInfo;
import org.sam.shen.core.util.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author suoyao
 * @date 2018年8月2日 上午11:16:49
  *  客户机Agent配置
 */
@Configuration
@ConfigurationProperties(prefix = "scheduing")
//@ComponentScan(basePackages = {"org.sam.shen.agent.service.handler", "org.sam.shen.core.handler"})
public class AgentConfig {
	private Logger logger = LoggerFactory.getLogger(AgentConfig.class);
	
	@Value("${agent.ip}")
	private String agentIp;
	
	@Value("${agent.name}")
	private String agentName;
	
	@Value("${agent.port}")
	private int agentPort;
	
	@Value("${agent.logpath}")
	private String logPath;
	
	@Value("${scheduing.server}")
	private String scheduingServer;

	@Value("${agent.shpath}")
	private String shPath;
	
	@Bean(initMethod="start", destroyMethod = "destroy")
	public RadishAgent ssyJobAgent() throws Exception {
        logger.info(">>>>>>>>>>> radish-agent config init.");

        RadishAgent radishAgent = new RadishAgent();
        radishAgent.setScheduingServer(scheduingServer);
        radishAgent.setLogPath(logPath);
        radishAgent.setShPath(shPath);

        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setAgentIp(agentIp);
        if (StringUtils.isEmpty(agentName)) {
            agentName = IpUtil.getHostName();
        }
        agentInfo.setAgentName(agentName);
        agentInfo.setAgentPort(agentPort);
        radishAgent.setAgentInfo(agentInfo);
        return radishAgent;
    }
}
