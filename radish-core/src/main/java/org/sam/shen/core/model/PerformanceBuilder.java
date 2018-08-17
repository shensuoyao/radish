package org.sam.shen.core.model;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.util.IpUtil;
import org.sam.shen.core.util.SystemUtil;
import org.sam.shen.core.util.SystemUtil.Binary;
import org.sam.shen.core.util.SystemUtil.Memory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author suoyao
 * @date 2018年7月31日 下午3:50:18
 *  Agent Performance Builder
 */
public class PerformanceBuilder {
	
	private static Logger logger = LoggerFactory.getLogger(PerformanceBuilder.class);
	
	private AgentPerformance performance;
	
	public PerformanceBuilder() {
		this.performance = new AgentPerformance();
		performance.setCpuCount(SystemUtil.cpuCount());
		if(StringUtils.isEmpty(performance.getIp())) {
			performance.setIp(IpUtil.getIp());
		}
		performance.setOsName(SystemUtil.osName());
		performance.setOsVersion(SystemUtil.osVersion());
		performance.setAgentName(IpUtil.getHostName());
	}
	
	public PerformanceBuilder(Long agentId) {
		this();
		performance.setAgentId(agentId);
	}
	
	public PerformanceBuilder(Long agentId, String agentName) {
		this(agentId);
		performance.setAgentName(agentName);
	}
	
	public PerformanceBuilder addAgentName(String agentName) {
		performance.setAgentName(agentName);
		return this;
	}
	
	/**
	 * @author suoyao
	 * @date 下午4:28:32
	 * @return
	  * 构建内存信息
	 */
	public PerformanceBuilder buildMemory() {
		// 计算JVM内存
		performance.setJvmTotalMemory(SystemUtil.jvmMemory(Memory.TOTAL, Binary.KB));
		performance.setJvmFreeMemory(SystemUtil.jvmMemory(Memory.FREE, Binary.KB));
		performance.setJvmMaxMemory(SystemUtil.jvmMemory(Memory.MAX, Binary.KB));
		
		// 计算物理内存
		performance.setPhysicalTotalMemory(SystemUtil.physicalMemory(Memory.TOTAL, Binary.KB));
		performance.setPhysicalFreeMemory(SystemUtil.physicalMemory(Memory.FREE, Binary.KB));
		
		return this;
	}
	
	/**
	 * @author suoyao
	 * @date 下午4:28:52
	 * @return
	  * 构建线程数
	 */
	public PerformanceBuilder buildThread() {
		ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
		while (threadGroup.getParent() != null) {
			threadGroup = threadGroup.getParent();
		}
		performance.setTotalThread(threadGroup.activeCount());
		return this;
	}
	
	public AgentPerformance build() {
		return performance;
	}
	
	public static void main(String[] args) {
		logger.info(new PerformanceBuilder().buildMemory().buildThread().build().toString());
	}
}
