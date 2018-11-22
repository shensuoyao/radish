package org.sam.shen.core.thread;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.agent.RadishAgent;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.event.HandlerEvent;
import org.sam.shen.core.handler.IHandler;
import org.sam.shen.core.model.Resp;
import org.sam.shen.core.rpc.RestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

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
			Resp<String> initRsp = handler.init();
			if(null == initRsp) {
				logger.error("Handler Init Method Return Null.");
				RestRequest.post(rpcReportUrl, new Resp<String>(1, "Handler Init Method Return Null."), eventId);
				return;
			}
			if (initRsp.getCode() != Resp.SUCCESS.getCode()) {
				// 失败上报终止继续执行
				RestRequest.post(rpcReportUrl, initRsp, eventId);
				return;
			}
            Resp<String> resp = handler.start(event);
            // 上报执行结果
            RestRequest.post(rpcReportUrl, resp, eventId);
		} catch (Exception e) {
			logger.error("Start Handler Error.", e);
			// 上报出错信息
			try {
				RestRequest.post(rpcReportUrl, new Resp<String>(1, "Start Handler Error.", e.getMessage()), eventId);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} finally {
			handler.destory();
			close();
		}
	}
	
	public synchronized boolean runable() {
		if(StringUtils.isEmpty(event.getRegistryHandler())) {
			// 处理器为空
			return Boolean.FALSE;
		}
		List<String> registryHandlers = Splitter.on(Constant.SPLIT_CHARACTER2).splitToList(event.getRegistryHandler());
		
		IHandler registryHandler = null;
		for(String regHandler : registryHandlers) {
			registryHandler = RadishAgent.loadJobHandler(regHandler);
			if(null == registryHandler) {
				continue;
			}
			this.handler = registryHandler;
			EventHandlerThreadPool.registryCallbackThread(this);
			EventHandlerThreadPool.registryHandlerNow(event.getRegistryHandler(), handler);
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
	public void close() {
		this.handler = null;
		EventHandlerThreadPool.unRegistryCallback(eventId);
		EventHandlerThreadPool.loadHandlerNow(event.getRegistryHandler());
	}
	
}
