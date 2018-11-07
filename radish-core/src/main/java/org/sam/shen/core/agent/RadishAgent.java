package org.sam.shen.core.agent;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.handler.IHandler;
import org.sam.shen.core.handler.anno.AHandler;
import org.sam.shen.core.log.RadishLogFileAppender;
import org.sam.shen.core.model.AgentInfo;
import org.sam.shen.core.model.Resp;
import org.sam.shen.core.rpc.RestRequest;
import org.sam.shen.core.thread.AgentHeartBeatThread;
import org.sam.shen.core.thread.TriggerEventThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

/**
 * @author suoyao
 * @date 2018年7月31日 下午5:43:52
 * Agent 客户端启动器
 */
@Component
public class RadishAgent implements ApplicationContextAware {
	
	private static Logger logger = LoggerFactory.getLogger(RadishAgent.class);

	private static ApplicationContext applicationContext;
	
	private static AgentInfo agentInfo;
	
	private static String scheduingServer;
	
	private static String logPath;

	private static String shPath;

	private static String shFilePath;
	
	public static AgentInfo getAgentInfo() {
		return agentInfo;
	}

	public void setAgentInfo(AgentInfo agentInfo) {
		RadishAgent.agentInfo = agentInfo;
	}
	
	public String getScheduingServer() {
		return scheduingServer;
	}

	public void setScheduingServer(String scheduingServer) {
		RadishAgent.scheduingServer = scheduingServer;
	}

	public static String getLogPath() {
		return logPath;
	}

	public void setLogPath(String logPath) {
	    if (StringUtils.isEmpty(logPath)) {
            RadishAgent.logPath = Constant.DEFAULT_LOG_FILE_PATH;
        } else {
            RadishAgent.logPath = logPath;
        }
	}

    public static String getShPath() {
        return shPath;
    }

    public void setShPath(String shPath) {
	    if (StringUtils.isEmpty(shPath)) {
            RadishAgent.shPath = Constant.DEFAULT_SHELL_SCRIPT_FILE_PATH;
        } else {
            RadishAgent.shPath = shPath;
        }
    }

    public static String getShFilePath() {
        return shFilePath;
    }

    @Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		RadishAgent.applicationContext = applicationContext;
	}
	
	public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
	
	public void start() throws Exception {
		// 初始化 Agent registry
		int exit = initHandlerRegistry(applicationContext);
		if(Constant.SUCCESS_EXIT != exit) {
			logger.error("Agent Registry Failed.");
			System.exit(0);
		}

		// 初始化任务日志
		RadishLogFileAppender.initLogPath(logPath);

		// 初始化shell脚本目录
        RadishLogFileAppender.initShPath(shPath);

        // 生成shell脚本并赋予执行权限
        createAndAuthShellScript();
		
		// 建立心跳
		initHeartBeat();

		// 抢占任务
		initTriggerCallback();
	}
	
	// ---------------------- Init Agent Handler And Registry ----------------------
	
	private static ConcurrentHashMap<String, IHandler> handlerRepository = new ConcurrentHashMap<String, IHandler>();
	
	public static IHandler registJobHandler(String name, IHandler jobHandler) {
		logger.info(">>>>>>>>>>> radish register handler success, name:{}, jobHandler:{}", name, jobHandler);
		return handlerRepository.put(name, jobHandler);
	}
	
	public static IHandler loadJobHandler(String name) {
		return handlerRepository.get(name);
	}
	
	public static void registryAgentInfoHandler(String name, String description) {
		if(null == agentInfo.getRegistryHandlerMap() || agentInfo.getRegistryHandlerMap().isEmpty()) {
			agentInfo.setRegistryHandlerMap(Maps.newHashMap());
		}
		agentInfo.getRegistryHandlerMap().put(name, description);
	}
	
	/**
	 * @author suoyao
	 * @date 下午5:47:42
	 * @return
	 *  初始化Agent 注册机制
	 *  0 表示成功, 其他表示错误
	 */
	public static int initHandlerRegistry(ApplicationContext applicationContext) {
		if(null == applicationContext) {
			logger.error("aplicationContext is not ready.");
			return 1;
		}
		Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(AHandler.class);
		if (serviceBeanMap != null && serviceBeanMap.size() > 0) {
			for (Object serviceBean : serviceBeanMap.values()) {
				if (serviceBean instanceof IHandler) {
					String name = serviceBean.getClass().getAnnotation(AHandler.class).name();
					String desc = serviceBean.getClass().getAnnotation(AHandler.class).description();
					IHandler handler = (IHandler) serviceBean;
					if (loadJobHandler(name) != null) {
						throw new RuntimeException("radish jobhandler naming conflicts.");
					}
					registJobHandler(name, handler);
					registryAgentInfoHandler(name, desc);
				}
			}
			
			// Registry Agent to Scheduing
			try {
				Resp<Long> resp = RestRequest.exchange(scheduingServer.concat("/core/registry"), HttpMethod.PUT, agentInfo, Long.class);
				if(Resp.SUCCESS.getCode() == resp.getCode()) {
					agentInfo.setAgentId(resp.getData());
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				return 1;
			}
		}
		return Constant.SUCCESS_EXIT;
	}
	
	// ---------------------- Init Agent HeartBeat ----------------------
	public void initHeartBeat() {
		AgentHeartBeatThread.getInstance().start(scheduingServer.concat("/core/heartbeat"), agentInfo.getAgentId(),
		        agentInfo.getAgentName());
	}
	
	// ---------------------- Init Agent Trigger Callback ----------------------
	public void initTriggerCallback() {
		TriggerEventThread.getInstance().start(scheduingServer.concat("/core/trigger-event/{agentId}"),
		        scheduingServer.concat("/core/handler-event-report/{eventId}"), agentInfo.getAgentId());
	}

	private void createAndAuthShellScript() {
	    shFilePath = shPath.endsWith(File.separator) ? shPath.concat(Constant.SHELL_SCRIPT_NAME) : shPath.concat(File.separator).concat(Constant.SHELL_SCRIPT_NAME);
	    // 生成shell脚本
	    try {
            File file = new File(shFilePath);
            ClassPathResource cpr = new ClassPathResource(Constant.SHELL_SCRIPT_NAME);
            if (file.exists() && !file.delete()) {
                throw new IOException("Can't Delete Existed File");
            }
            if (!file.createNewFile()) {
                throw new IOException("Can't Create New File");
            }
            FileUtils.copyInputStreamToFile(cpr.getInputStream(), file);
        } catch (IOException e) {
	        logger.error("Create Shell Script File Failed. [{}]", e);
        }

        // 赋予shell脚本执行权限
        try {
            ProcessBuilder pb = new ProcessBuilder("/bin/chmod", "755", shFilePath);
            Process process = pb.start();
            process.waitFor();
        } catch (Exception e) {
            logger.error("Auth Shell Script File Failed. [{}]", e);
        }
    }
	
	public void destroy() {
		// TODO
		logger.info("Destory RadishAgent ...");
	}

}
