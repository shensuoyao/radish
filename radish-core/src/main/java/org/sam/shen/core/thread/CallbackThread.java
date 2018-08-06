package org.sam.shen.core.thread;

import org.sam.shen.core.agent.RadishAgent;
import org.sam.shen.core.handler.CallBackParam;
import org.sam.shen.core.handler.IHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author suoyao
 * @date 2018年8月2日 下午2:06:44
  *  执行任务线程
 */
public class CallbackThread extends Thread {
	Logger logger = LoggerFactory.getLogger(CallbackThread.class);
	
	private String jobId;
	
	private CallBackParam callbackParam;
	
	private IHandler handler;
	
	public String getJobId() {
		return jobId;
	}

	public CallbackThread(CallBackParam callbackParam) {
		this.callbackParam = callbackParam;
		this.jobId = callbackParam.getJobId();
	}
	
	@Override
	public void run() {
		if(!runable()) {
			return;
		}
		try {
			handler.init();
			handler.start(callbackParam);
		} catch (Exception e) {
			logger.error("Start Handler Error.", e);
		} finally {
			handler.destory();
			close();
		}
	}
	
	public boolean runable() {
		IHandler registryHandler = RadishAgent.loadJobHandler(callbackParam.getRegistryHandler());
		if(null == registryHandler) {
			logger.error("Callback Handler Is Not Found. {}", callbackParam.getRegistryHandler());
			return Boolean.FALSE;
		}
		this.handler = registryHandler;
		CallbackThreadPool.registryCallbackThread(this);
		CallbackThreadPool.registryHandlerNow(callbackParam.getRegistryHandler(), handler);
		return Boolean.TRUE;
	}
	
	public void close() {
		this.handler = null;
		CallbackThreadPool.unRegistryCallback(jobId);
		CallbackThreadPool.loadHandlerNow(callbackParam.getRegistryHandler());
	}
	
}
