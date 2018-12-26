package org.sam.shen.core.thread;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.agent.RadishAgent;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.event.HandlerEvent;
import org.sam.shen.core.handler.AbsHandler;
import org.sam.shen.core.handler.IHandler;
import org.sam.shen.core.model.Resp;
import org.sam.shen.core.rpc.RestRequest;

import com.google.common.base.Splitter;

/**
 * @author suoyao
 * @date 2018年8月2日 下午2:06:44
  *  执行任务线程
 */
@Slf4j
public class EventHandlerThread extends Thread {

	private String rpcReportUrl;

    private String rpcSubeventUrl;
	
	private String eventId;
	
	private HandlerEvent event;
	
	private IHandler handler;

	public String getEventId() {
		return eventId;
	}

	public EventHandlerThread(HandlerEvent event, String rpcSubeventUrl, String rpcReportUrl) {
	    this.rpcSubeventUrl = rpcSubeventUrl;
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
				log.error("Handler Init Method Return Null.");
				event.setHandlerResult(new Resp<>(1, "Handler Init Method Return Null."));
                RestRequest.post(rpcReportUrl, event);
                return;
			}
			if (initRsp.getCode() != Resp.SUCCESS.getCode()) {
				// 失败上报终止继续执行
                event.setHandlerResult(initRsp);
				RestRequest.post(rpcReportUrl, event);
				return;
			}
            Resp<String> resp = handler.start(event);
			// 如果handler继承AbsHandler，设置handler执行日志路径，用于保存到数据库
			if (handler instanceof AbsHandler) {
                event.setHandlerLogPath(((AbsHandler) handler).getLogFileName());
            }
            // 上报执行结果
            event.setHandlerResult(resp);
            RestRequest.post(rpcReportUrl, event);
			// 如果任务执行成功，查看是否存在子任务
			if (resp.getCode() == Resp.SUCCESS.getCode()) {
			    RestRequest.post(rpcSubeventUrl, event);
            }
		} catch (Exception e) {
			log.error("Start Handler Error.", e);
			// 上报出错信息
			try {
			    event.setHandlerResult(new Resp<>(1, "Start Handler Error.", e.getMessage()));
				RestRequest.post(rpcReportUrl, event);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} finally {
			handler.destroy();
			close();
		}
	}
	
	public synchronized boolean runable() {
		if(StringUtils.isEmpty(event.getRegistryHandler())) {
			// 处理器为空
			return Boolean.FALSE;
		}
		List<String> registryHandlers = Splitter.on(Constant.SPLIT_CHARACTER2).splitToList(event.getRegistryHandler());
		
		IHandler registryHandler;
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
