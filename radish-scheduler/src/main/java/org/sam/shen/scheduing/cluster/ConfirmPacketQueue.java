package org.sam.shen.scheduing.cluster;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringUtils;

/**
 * 数据包确认队列，防止数据未被处理的重试机制
 * @author suoyao
 * @date 2019年3月20日 下午1:56:10
  * 
 */
public class ConfirmPacketQueue {

	private LinkedBlockingQueue<String> confirmQueue;
	
	private ConcurrentHashMap<String, ClusterPacket<LeaderInfo>> lastConfirmPacket;
	
	public ConfirmPacketQueue() {
		this.confirmQueue = new LinkedBlockingQueue<>();
		this.lastConfirmPacket = new ConcurrentHashMap<>();
	}
	
	/*
	 * 添加需要确认的事务数据包
	 */
	public void addConfirmPacket(ClusterPacket<LeaderInfo> packet) {
		confirmQueue.add(packet.getUxid());
		lastConfirmPacket.put(packet.getUxid(), packet);
	}
	
	/*
	 * 移除需要确认的事务数据包
	 */
	public void removeConfirmPacket(String uxid) {
		confirmQueue.remove(uxid);
		lastConfirmPacket.remove(uxid);
	}
	
	/*
	 * 从队列中获取需要重发的packet信息
	 */
	public ClusterPacket<LeaderInfo> takeConfirmPacket() throws InterruptedException {
		String uxid = confirmQueue.take();
		if (lastConfirmPacket.containsKey(uxid)) {
			return lastConfirmPacket.get(uxid);
		} else {
			confirmQueue.remove(uxid);
			return null;
		}
	}
	
	public ClusterPacket<LeaderInfo> pollConfirmPacket() {
		String uxid = confirmQueue.poll();
		if(StringUtils.isEmpty(uxid)) {
			return null;
		}
		if (lastConfirmPacket.containsKey(uxid)) {
			return lastConfirmPacket.get(uxid);
		} else {
			confirmQueue.remove(uxid);
			return null;
		}
	}
	
}
