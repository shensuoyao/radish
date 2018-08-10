package org.sam.shen.core.thread;

import java.util.concurrent.TimeUnit;

import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.handler.CallBackParam;
import org.sam.shen.core.model.Resp;
import org.sam.shen.core.rpc.RestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author suoyao
 * @date 2018年8月1日 下午5:06:18
  *  触发任务线程
 */
public class TriggerCallbackThread {
	private static Logger logger = LoggerFactory.getLogger(TriggerCallbackThread.class);
	
	private static TriggerCallbackThread instance = new TriggerCallbackThread();

	public static TriggerCallbackThread getInstance() {
		return instance;
	}
	
	/**
	 * 触发抢占任务线程
	 */
	private Thread triggerThread;
	
	/**
	 *  任务执行线程
	 */
	private Thread callbckThread;
	
	private volatile boolean toStop = false;
	
	public void start(String rpcUrl, String agentName) {
		triggerThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(!toStop) {
					try {
						logger.info(" Callback Queue size is: {}", CallbackThreadPool.callbackQueueSize());
						if (CallbackThreadPool.isCallbackQueueFull()) {
							logger.error("Callback Queue is Full.");
						} else {
							Resp<CallBackParam> resp = RestRequest.get(rpcUrl, CallBackParam.class, "agentName=".concat(agentName));
							if(Resp.SUCCESS.getCode() == resp.getCode()) {
								logger.info("Agent Trigger Callback {}", resp.getData().toString());
								
								CallbackThreadPool.pushCallbackQueue(resp.getData());
							}
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
					
					try {
						TimeUnit.SECONDS.sleep(Constant.BEAT_TIMEOUT);
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		});
		triggerThread.setDaemon(true);
		triggerThread.start();
		
		// 启动任务线程队列
		callbckThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(!toStop) {
					if(CallbackThreadPool.isAvailable()) {
						CallbackThreadPool.run();
					} else {
						logger.error("CallbackThreadPool is Full.");
					}
					
					try {
						TimeUnit.SECONDS.sleep(Constant.BEAT_TIMEOUT);
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		});
		callbckThread.setDaemon(true);
		callbckThread.start();
		
	}
	
	public void toStop() {
		toStop = true;
		// stop trigger & callback, interrupt and wait
		triggerThread.interrupt();
		callbckThread.interrupt();
		try {
			triggerThread.join();
			callbckThread.join();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
