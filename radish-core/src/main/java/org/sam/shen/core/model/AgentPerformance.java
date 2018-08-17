package org.sam.shen.core.model;

import java.io.Serializable;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author suoyao
 * @date 2018年7月31日 下午3:31:05
 * Agent performance
 */
public class AgentPerformance implements Serializable {

	private static final long serialVersionUID = 3571458226767022113L;

	/**
	 *  客户端唯一Id
	 */
	private Long agentId;
	
	/**
	 * 客户端名称
	 */
	private String agentName;
	
	/**
	 * 本机IP
	 */
	private String ip;
	
	/**
	 *  操作系统名称
	 */
	private String osName;
	
	/**
	 * 操作系统版本
	 */
	private String osVersion;
	
	/**
	 * CPU核心数
	 */
	private int cpuCount;
	
	/**
	 * CPU使用率
	 */
	private double cpuRatio;
	
	/**
	 * JVM总内存
	 */
	private Long jvmTotalMemory;
	
	/**
	 * JVM空闲内存
	 */
	private Long jvmFreeMemory;
	
	/**
	 *  JVM 最大内存
	 */
	private Long jvmMaxMemory;
	
	/**
	 * 物理总内存
	 */
	private Long physicalTotalMemory;
	
	/**
	 * 物理空闲内存
	 */
	private Long physicalFreeMemory;
	
	/**
	 * 总线程数
	 */
	private int totalThread;

	public Long getAgentId() {
		return agentId;
	}

	public void setAgentId(Long agentId) {
		this.agentId = agentId;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getCpuCount() {
		return cpuCount;
	}

	public void setCpuCount(int cpuCount) {
		this.cpuCount = cpuCount;
	}

	public double getCpuRatio() {
		return cpuRatio;
	}

	public void setCpuRatio(double cpuRatio) {
		this.cpuRatio = cpuRatio;
	}

	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public Long getJvmTotalMemory() {
		return jvmTotalMemory;
	}

	public void setJvmTotalMemory(Long jvmTotalMemory) {
		this.jvmTotalMemory = jvmTotalMemory;
	}

	public Long getJvmFreeMemory() {
		return jvmFreeMemory;
	}

	public void setJvmFreeMemory(Long jvmFreeMemory) {
		this.jvmFreeMemory = jvmFreeMemory;
	}

	public Long getJvmMaxMemory() {
		return jvmMaxMemory;
	}

	public void setJvmMaxMemory(Long jvmMaxMemory) {
		this.jvmMaxMemory = jvmMaxMemory;
	}

	public Long getPhysicalTotalMemory() {
		return physicalTotalMemory;
	}

	public void setPhysicalTotalMemory(Long physicalTotalMemory) {
		this.physicalTotalMemory = physicalTotalMemory;
	}

	public Long getPhysicalFreeMemory() {
		return physicalFreeMemory;
	}

	public void setPhysicalFreeMemory(Long physicalFreeMemory) {
		this.physicalFreeMemory = physicalFreeMemory;
	}

	public int getTotalThread() {
		return totalThread;
	}

	public void setTotalThread(int totalThread) {
		this.totalThread = totalThread;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteNullStringAsEmpty,
		        SerializerFeature.WriteNullNumberAsZero);
	}
	
}
