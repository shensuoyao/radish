package org.sam.shen.core.thread;

import org.sam.shen.core.agent.RadishAgent;
import org.sam.shen.core.event.HandlerEvent;
import org.sam.shen.core.handler.IHandler;
import org.sam.shen.core.model.Resp;
import org.sam.shen.core.rpc.RestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author suoyao
 * @date 2018年8月2日 下午2:06:44
  *  执行任务线程
 */
public class EventHandlerThread extends Thread {
	Logger logger = LoggerFactory.getLogger(EventHandlerThread.class);
	
	private String rpcReportUrl;
	
	private String eventId;
	
	private HandlerEvent event;
	
	private IHandler handler;

	public String getEventId() {
		return eventId;
	}

	public EventHandlerThread(HandlerEvent event, String rpcReportUrl) {
		this.rpcReportUrl = rpcReportUrl;
		this.event = event;
		this.eventId = event.getEventId();
	}
	
	@Override
	public void run() {
		if(!runable()) {
			return;
		}
		try {
			handler.init();
			Resp<String> resp = handler.start(event);
			RestRequest.post(rpcReportUrl, resp, eventId);    // 上报执行结果
		} catch (Exception e) {
			logger.error("Start Handler Error.", e);
			// 上报出错信息
			try {
				RestRequest.post(rpcReportUrl, Resp.FAIL, eventId);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
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
		EventHandlerThreadPool.registryCallbackThread(this);
		EventHandlerThreadPool.registryHandlerNow(event.getRegistryHandler(), handler);
		return Boolean.TRUE;
	}
	
	public void close() {
		this.handler = null;
		EventHandlerThreadPool.unRegistryCallback(eventId);
		EventHandlerThreadPool.loadHandlerNow(event.getRegistryHandler());
	}
	
}
