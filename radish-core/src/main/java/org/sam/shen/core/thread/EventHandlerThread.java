package org.sam.shen.core.thread;

import org.sam.shen.core.agent.RadishAgent;
import org.sam.shen.core.event.HandlerEvent;
import org.sam.shen.core.handler.IHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author suoyao
 * @date 2018年8月2日 下午2:06:44
  *  执行任务线程
 */
public class EventHandlerThread extends Thread {
	Logger logger = LoggerFactory.getLogger(EventHandlerThread.class);
	
	private String callId;
	
	private HandlerEvent event;
	
	private IHandler handler;

	public String getCallId() {
		return callId;
	}

	public EventHandlerThread(HandlerEvent event) {
		this.event = event;
		this.callId = event.getEventId();
	}
	
	@Override
	public void run() {
		if(!runable()) {
			return;
		}
		try {
			handler.init();
			handler.start(event);
		} catch (Exception e) {
			logger.error("Start Handler Error.", e);
		} finally {
			handler.destory();
			close();
		}
	}
	
	public boolean runable() {
		IHandler registryHandler = RadishAgent.loadJobHandler(event.getRegistryHandler());
		if(null == registryHandler) {
			logger.error("Callback Handler Is Not Found. {}", event.getRegistryHandler());
			return Boolean.FALSE;
		}
		this.handler = registryHandler;
		CallbackThreadPool.registryCallbackThread(this);
		CallbackThreadPool.registryHandlerNow(event.getRegistryHandler(), handler);
		return Boolean.TRUE;
	}
	
	public void close() {
		this.handler = null;
		CallbackThreadPool.unRegistryCallback(callId);
		CallbackThreadPool.loadHandlerNow(event.getRegistryHandler());
	}
	
}
