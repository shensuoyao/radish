package org.sam.shen.core.thread;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.model.Monitor;
import org.sam.shen.core.model.Resp;
import org.sam.shen.core.rpc.RestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

/**
 * @author suoyao
 * @date 2018年8月1日 下午2:58:02
  *  Agent 客户端心跳执行线程
 */
public class AgentHeartBeatThread extends Thread {

	Logger logger = LoggerFactory.getLogger(AgentHeartBeatThread.class);
	
	private static AgentHeartBeatThread instance = new AgentHeartBeatThread();
	
	public static AgentHeartBeatThread getInstance() {
		return instance;
	}
	
	/**
	 * 心跳线程
	 */
	private Thread beatThread;
	
	private volatile boolean toStop = false;
	
	public void start(final String rpcUrl, Long agentId, String agentName) {
		//必须设置客户端名称, 否则无法判断是哪一台机器的心跳
		if(StringUtils.isEmpty(agentName)) {
			logger.warn(">>>>>>>>>>> radish, agent heartbeat fail, agentName is null.");
		}
		
		Monitor monitor = new Monitor(agentId, agentName);
		
		beatThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// HeartBeat
				while(!toStop) {
					try {
						Resp<Object> resp = RestRequest.post(rpcUrl, monitor.collect(Monitor.MonitorType.All));
						if(Resp.SUCCESS.getCode() != resp.getCode()) {
							logger.error(JSON.toJSONString(resp));
							logger.error("HeartBeat failed the reason is: {}, detail: {}", resp.getMsg(), resp.getData());
						}
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}
					try {
						TimeUnit.SECONDS.sleep(Constant.BEAT_TIMEOUT);
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
				}
				// 心跳停止, Agent客户端下线, 暂停, 停止
				// 销毁资源
				// TODO
			}
		});
		beatThread.setDaemon(true);
		beatThread.start();
	}
	
	public boolean isStop() {
		return toStop;
	}
	
	/**
	 * @author suoyao
	 * @date 下午3:07:53
	 * 停止心跳
	 */
	public void toStop() {
		toStop = true;
		// interrupt and wait
		beatThread.interrupt();
		try {
			beatThread.join();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
