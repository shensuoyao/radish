package org.sam.shen.core.util;

import java.lang.management.ManagementFactory;
import java.util.Properties;

import com.sun.management.OperatingSystemMXBean;

/**
 * @author suoyao
 * @date 2018年8月2日 上午10:40:00
  *  系统工具
 */
public class SystemUtil {

	public static final int KB = 1024;
	
	private static Runtime runtime = Runtime.getRuntime();
	private static Properties sysProperties = System.getProperties();
	
	public static int cpuCount() {
		return runtime.availableProcessors();
	}
	
	public static String osName() {
		return sysProperties.getProperty("os.name");
	}
	
	public static String osVersion() {
		return sysProperties.getProperty("os.version");
	}
	
	/**
	 * @author suoyao
	 * @date 上午11:02:04
	 * @param memory
	 * @param binary
	 * @return
	 *  获取JVM内存
	 */
	public static Long jvmMemory(Memory memory, Binary binary) {
		Long mem = 0L;
		switch(memory) {
		case TOTAL:
			mem = runtime.totalMemory();
			break;
		case FREE:
			mem = runtime.freeMemory();
			break;
		case MAX:
			mem = runtime.maxMemory();
			break;
		default:
			break;
		}
		switch (binary) {
		case MB:
			return mem / KB / KB;
		case GB:
			return mem / KB / KB / KB;
		default:
			return mem / KB;
		}
	}
	
	/**
	 * @author suoyao
	 * @date 上午11:08:18
	 * @param memory
	 * @param binary
	 * @return
	 *  获取物理内存
	 */
	public static Long physicalMemory(Memory memory, Binary binary) {
		OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		Long mem = 0L;
		switch(memory) {
		case TOTAL:
			mem = osmxb.getTotalPhysicalMemorySize();
			break;
		case FREE:
			mem = osmxb.getFreePhysicalMemorySize();
			break;
		default:
			mem = osmxb.getFreePhysicalMemorySize();
			break;
		}
		switch (binary) {
		case MB:
			return mem / KB / KB;
		case GB:
			return mem / KB / KB / KB;
		default:
			return mem / KB;
		}
	}
	
	public static enum Binary {
		KB, MB, GB;
	}
	
	public static enum Memory {
		TOTAL, FREE, MAX;
	}
	
}
