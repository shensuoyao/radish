package org.sam.shen.core.thread;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.event.HandlerEvent;
import org.sam.shen.core.handler.IHandler;
import org.sam.shen.core.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author suoyao
 * @date 2018年8月2日 下午12:57:46
  *  执行处理业务线程池
 */
public class EventHandlerThreadPool {
	private static Logger logger = LoggerFactory.getLogger(EventHandlerThreadPool.class);

	private static ThreadPoolExecutor fixedThreadPool;
	
	/**
	 *  执行任务线程注册容器
	 */
	private static ConcurrentHashMap<String, EventHandlerThread> callbackThreadRepository = new ConcurrentHashMap<>();
	
	static {
		fixedThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(SystemUtil.cpuCount() + 1);
	}
	
	/**
	 * 检查线程池是否可用
	 * @author suoyao
	 * @date 下午5:07:24
	 * @return
	 */
	public static synchronized boolean isAvailable() {
		int avalibleCount = (fixedThreadPool.getMaximumPoolSize() - fixedThreadPool.getActiveCount());
		if(logger.isDebugEnabled()) {
			logger.info("Callback Thread Pool avaliavle size is: {}", avalibleCount);
		}
		return avalibleCount > 0;
	}
	
	public static void run(String rpcSubeventUrl, String rpcReportUrl) {
		HandlerEvent handlerEvent = takeCallbackQueue();
		if(null != handlerEvent && StringUtils.isNotEmpty(handlerEvent.getEventId())) {
			fixedThreadPool.execute(new EventHandlerThread(handlerEvent, rpcSubeventUrl, rpcReportUrl));
		}
	}
	
	public static void registryCallbackThread(EventHandlerThread eventHandlerThread) {
		callbackThreadRepository.put(eventHandlerThread.getEventId(), eventHandlerThread);
	}
	
	/**
	 * @author suoyao
	 * @date 下午12:46:50
	 * @param eventId
	  *   卸载注册的执行线程
	 */
	public static void unRegistryCallback(String eventId) {
		loadCallbackThread(eventId);
	}
	
	public static EventHandlerThread loadCallbackThread(String eventId) {
		return callbackThreadRepository.remove(eventId);
	}
	
	public static void stopCallbackThread(String eventId) {
		EventHandlerThread callbackThread = loadCallbackThread(eventId);
		callbackThread.interrupt();
	}
	
	/**
	 * 等待执行的任务队列
	 */
	private static LinkedBlockingQueue<HandlerEvent> eventsQueue = new LinkedBlockingQueue<>(SystemUtil.cpuCount() * 3);
	
	/**
	 * 正在执行任务的 Handler
	 */
	private static ConcurrentHashMap<String, IHandler> handlerNow = new ConcurrentHashMap<String, IHandler>();
	
	/**
	 * @author suoyao
	 * @date 下午4:29:05
	 * @return
	 *  检查任务执行队列是否满的
	 */
	public static boolean isCallbackQueueFull() {
		return eventsQueue.remainingCapacity() <= 0;
	}
	
	public static int callbackQueueSize() {
		return eventsQueue.size();
	}
	
	public static IHandler loadHandlerNow(String registryHandler) {
		return handlerNow.remove(registryHandler);
	}

	public static void registryHandlerNow(String registryHandler, IHandler handler) {
		handlerNow.put(registryHandler, handler);
	}

	/**
	  *  将等待执行的任务放入队列中
	 * @author suoyao
	 * @date 下午3:46:05
	 * @param event
	 */
	public static void pushCallbackQueue(HandlerEvent event) {
		eventsQueue.add(event);
	}
	
	/**
	 * @author suoyao
	 * @date 下午3:54:08
	 * @return
	 *  从队列中获取需要执行的任务
	 */
	public static HandlerEvent takeCallbackQueue() {
		synchronized (eventsQueue) {
			HandlerEvent handlerEvent = null;
			try {
				handlerEvent = eventsQueue.peek();
				if(null == handlerEvent) {
					return null;
				}
				if(handlerNow.containsKey(handlerEvent.getRegistryHandler())) {
					return null;
				}
				return eventsQueue.take();
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}
	
}
