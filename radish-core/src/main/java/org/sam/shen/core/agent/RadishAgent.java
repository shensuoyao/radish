package org.sam.shen.core.agent;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.handler.IHandler;
import org.sam.shen.core.handler.anno.AHandler;
import org.sam.shen.core.handler.impl.ScriptHandler;
import org.sam.shen.core.log.RadishLogFileAppender;
import org.sam.shen.core.model.AgentInfo;
import org.sam.shen.core.model.Resp;
import org.sam.shen.core.rpc.RestRequest;
import org.sam.shen.core.thread.AgentHeartBeatThread;
import org.sam.shen.core.thread.TriggerEventThread;
import org.sam.shen.core.util.IpUtil;
import org.sam.shen.core.util.ScriptUtil;
import org.sam.shen.core.util.SystemUtil;
import org.springframework.http.HttpMethod;

import com.google.common.collect.Maps;

/**
 * @author suoyao
 * @date 2018年7月31日 下午5:43:52
 * Agent 客户端启动器
 */
@Slf4j
public class RadishAgent {
	
	private static AgentInfo agentInfo;
	
	private static String scheduingServer;
	
	private static String logPath;

	private static String shPath;

	private static String shFilePath;

	private static List<IHandler> handlers;

	// 心跳间隔，单位毫秒
	private static long heartBeat = 2000;

	// 抢占事件时间间隔，单位毫秒
	private static long triggerBeat = 2000;

	// 处理事件时间间隔，单位毫秒
	private static long handleEventBeat = 1000;

	// 核心线程数
	private static int corePoolSize = SystemUtil.cpuCount() + 1;

	public RadishAgent() {
		// 初始化默认参数
		if (SystemUtil.osName().startsWith("Windows")) { // windows系统不能运行shell脚本，脚本目录无需设置
			logPath = Constant.DEFAULT_LOG_FILE_PATH_WIN;
		} else {
			logPath = Constant.DEFAULT_LOG_FILE_PATH_LINUX;
			shPath = Constant.DEFAULT_SHELL_SCRIPT_FILE_PATH;
		}
		agentInfo = new AgentInfo();
		agentInfo.setAgentName(IpUtil.getIp() == null ? null : ("agent_".concat(IpUtil.getIp())));
		agentInfo.setAgentIp(IpUtil.getIp());
		agentInfo.setAgentPort(Constant.DEFAULT_AGENT_PORT);
		agentInfo.setNetwork(Constant.AGENT_LOG_NETWORK_NETTY);
		agentInfo.setNettyPort(Constant.DEFAULT_NETTY_PORT);
		// 初始化handler
		handlers = new ArrayList<>();
		handlers.add(new ScriptHandler());
	}

	public RadishAgent(List<IHandler> handlers) {
	    this();
	    RadishAgent.handlers.addAll(handlers);
    }

	public void start() {
		// 初始化 Agent registry
		int exit = initHandlerRegistry();
		if(Constant.SUCCESS_EXIT != exit) {
			log.error("Agent Registry Failed.");
			System.exit(0);
		}

		// 初始化任务日志
		RadishLogFileAppender.initLogPath(logPath);

		if (!SystemUtil.osName().startsWith("Windows")) {
			// 初始化shell脚本目录
			RadishLogFileAppender.initShPath(shPath);
			// 生成shell脚本并赋予执行权限
			initMonitorScriptFile();
		}
		
		// 建立心跳
		initHeartBeat();

		// 抢占任务
		initTriggerCallback();
	}
	
	// ---------------------- Init Agent Handler And Registry ----------------------
	
	private static ConcurrentHashMap<String, IHandler> handlerRepository = new ConcurrentHashMap<String, IHandler>();
	
	public static IHandler registerJobHandler(String name, IHandler jobHandler) {
		log.info(">>>>>>>>>>> radish register handler success, name:{}, jobHandler:{}", name, jobHandler);
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
	 * 初始化Agent 注册机制
	 * 0 表示成功, 其他表示错误
	 */
	private int initHandlerRegistry() {
		if (handlers.size() > 0) {
			for (IHandler handler : handlers) {
                String name = handler.getClass().getAnnotation(AHandler.class).name();
                String desc = handler.getClass().getAnnotation(AHandler.class).description();
                IHandler curHandler = loadJobHandler(name);
                // 如果存在重复名称的handler，并且实现类不同，则抛出异常
                if (curHandler != null && !curHandler.getClass().isInstance(handler)) {
                    throw new RuntimeException("Radish job handler naming conflicts.");
                }
                if (curHandler == null) {
					registerJobHandler(name, handler);
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
				log.error(e.getMessage(), e);
				return 1;
			}
		}
		return Constant.SUCCESS_EXIT;
	}
	
    /**
     * Init Agent HeartBeat
     */
	private void initHeartBeat() {
		AgentHeartBeatThread.getInstance().start(scheduingServer.concat("/core/heartbeat"), agentInfo.getAgentId(),
		        agentInfo.getAgentName());
	}

    /**
     * Init Agent Trigger Callback
     */
    private void initTriggerCallback() {
		TriggerEventThread.getInstance().start(scheduingServer.concat("/core/trigger-event/{agentId}?count={count}"),
		        scheduingServer.concat("/core/handle-child-event"),
		        scheduingServer.concat("/core/handler-event-report"), agentInfo.getAgentId());
	}

    /**
     * Initialize monitor shell script file
     */
	private void initMonitorScriptFile() {
	    shFilePath = shPath.concat(File.separator).concat(Constant.SHELL_SCRIPT_NAME);
        URL fileUrl = this.getClass().getResource(Constant.SHELL_SCRIPT_PATH.concat(File.separator).concat(Constant.SHELL_SCRIPT_NAME));
	    try {
            ScriptUtil.createAndAuthShellScript(shFilePath, fileUrl.openStream());
        } catch (Exception e) {
	        log.error("Initialize monitor shell script file failed. [{}]", e.getMessage());
        }
    }
	
	public void destroy() {
		// TODO
		log.info("Destroy RadishAgent ...");
	}

    public AgentInfo getAgentInfo() {
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
		RadishAgent.logPath = logPath;
    }

    public static String getShPath() {
        return shPath;
    }

    public void setShPath(String shPath) {
		RadishAgent.shPath = shPath;
    }

    public static String getShFilePath() {
        return shFilePath;
    }

	public static long getHeartBeat() {
		return heartBeat;
	}

	public void setHeartBeat(long heartBeat) {
		RadishAgent.heartBeat = heartBeat;
	}

	public static long getTriggerBeat() {
		return triggerBeat;
	}

	public void setTriggerBeat(long triggerBeat) {
		RadishAgent.triggerBeat = triggerBeat;
	}

	public static long getHandleEventBeat() {
		return handleEventBeat;
	}

	public void setHandleEventBeat(long handleEventBeat) {
		RadishAgent.handleEventBeat = handleEventBeat;
	}

	public static int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		RadishAgent.corePoolSize = corePoolSize;
	}
}
