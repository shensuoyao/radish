package org.sam.shen.core.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author clock
 * @date 2018/10/30 下午12:13
 */
@Getter
@Setter
public class AgentMonitorInfo implements Serializable {

    private static final long serialVersionUID = -3919488232847843771L;

    @Getter
    public enum AgentStatus {
        NULL(0), FINE(1), COMMON(2), WARNING(3);

        private int value;

        AgentStatus(int value) {
            this.value = value;
        }

        public static AgentStatus fromValue(int value) {
            for (AgentStatus agentStatus : AgentStatus.values()) {
                if (agentStatus.getValue() == value) {
                    return agentStatus;
                }
            }
            return null;
        }
    }

    /**
     * 客户端唯一Id
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
     * 操作系统名称
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
     * 客户端运行状态
     */
    private AgentStatus agentStatus;

    /**
     * 用户级别运行CPU时间占比
     */
    @JSONField(name = "cpu.util.user")
    private Double cpuUser;

    /**
     * 系统级别运行CPU时间占比
     */
    @JSONField(name = "cpu.util.system")
    private Double cpuSystem;

    /**
     * 用户级别执行nice优先级操作CPU时间占比
     */
    @JSONField(name = "cpu.util.nice")
    private Double cpuNice;

    /**
     * IO等待时间占CPU运行时间的百分比
     */
    @JSONField(name = "cpu.util.iowait")
    private Double cpuIowait;

    /**
     * 管理程序为另一个虚拟机进程提供服务等待虚拟CPU的时间占CPU总时间的百分比
     */
    @JSONField(name = "cpu.util.steal")
    private Double cpuSteal;

    /**
     * 空闲时间占CPU运行总时间的百分比
     */
    @JSONField(name = "cpu.util.idle")
    private Double cpuIdle;

    /**
     * 空闲内存
     */
    @JSONField(name = "mem.kbmemfree")
    private Long memoryFree;

    /**
     * 已使用内存
     */
    @JSONField(name = "mem.kbmemused")
    private Long memoryUsed;

    /**
     * 缓冲区所使用的内存
     */
    @JSONField(name = "mem.kbmembuffers")
    private Long memoryBuffers;

    /**
     * 缓存所使用的内存
     */
    @JSONField(name = "mem.kbmemcached")
    private Long memoryCached;

    /**
     * 保证内存不溢出所需的内存
     */
    @JSONField(name = "mem.kbmemcommit")
    private Long memoryCommit;

    /**
     * 已用内存的百分比
     */
    @JSONField(name = "mem.memused")
    private Double memoryUsedUtil;

    /**
     * 保证内存不溢出所需内存的百分比
     */
    @JSONField(name = "mem.commit")
    private Double memoryCommitUtil;

    /**
     * 磁盘每秒的传输总次数
     */
    @JSONField(name = "io.tps")
    private Double ioTps;

    /**
     * 磁盘每秒的写入请求数
     */
    @JSONField(name = "io.wtps")
    private Double ioWtps;

    /**
     * 磁盘每秒的读取请求数
     */
    @JSONField(name = "io.rtps")
    private Double ioRtps;

    /**
     * 近1分钟的系统平均负载
     */
    @JSONField(name = "system.ldavg-1")
    private Double ldavg1;

    /**
     * 近5分钟的系统平均负载
     */
    @JSONField(name = "system.ldavg-5")
    private Double ldavg5;

    /**
     * 近15分钟的系统平均负载
     */
    @JSONField(name = "system.ldavg-15")
    private Double ldavg15;

    /**
     * php进程所占的物理内存
     */
    @JSONField(name = "php.rss")
    private Long phpRss;

    /**
     * 各个java服务占用的物理内存
     */
    private List<JavaMemory> javaMemoryList = new ArrayList<>();

    /**
     * 各网卡的io情况
     */
    private List<NetworkIO> networkIOList = new ArrayList<>();

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    @Getter
    @Setter
    public static class JavaMemory implements Serializable {
        private static final long serialVersionUID = 1595010069385273164L;

        /**
         * java启动类名
         */
        private String name;
        /**
         * java进程所占物理内存
         */
        private Long rss;
    }

    @Getter
    @Setter
    public static class NetworkIO implements Serializable {
        private static final long serialVersionUID = -5244193663281737881L;

        /**
         * 网卡接口
         */
        private String iface;
        /**
         * 每秒接受的数据量，KB
         */
        @JSONField(name = "rxkB")
        private Double rx;
        /**
         * 每秒传输的数据量，KB
         */
        @JSONField(name = "txkB")
        private Double tx;
    }


    public void computeAgentStatus() {
        AgentStatus cpuStatus = AgentStatus.NULL;
        if (cpuSystem != null && cpuUser != null) {
            double sysAndUser = cpuSystem + cpuUser;
            if (sysAndUser < 70) {
                cpuStatus = AgentStatus.FINE;
            } else if (sysAndUser >= 70 && sysAndUser < 90) {
                cpuStatus = AgentStatus.COMMON;
            } else if (sysAndUser >= 90) {
                cpuStatus = AgentStatus.WARNING;
            }
        }

        AgentStatus memStatus = AgentStatus.NULL;
        if (memoryUsed != null && memoryFree != null && memoryBuffers != null && memoryCached != null) {
            // 这里计算应用可用物理内存，buffers和cached表示被分配但未被使用的内存，当实际可用内存不足时，buffers和cached
            // 会释放一部分内存
            long memFree = memoryFree + memoryCached + memoryBuffers;
            long total = memoryFree + memoryUsed;
            double freeUtil = memFree * 100d / total;
            if (freeUtil > 70) {
                memStatus = AgentStatus.FINE;
            } else if (freeUtil <= 70 && freeUtil > 20) {
                memStatus = AgentStatus.COMMON;
            } else if (freeUtil <= 20) {
                memStatus = AgentStatus.WARNING;
            }
        }

        agentStatus = AgentStatus.fromValue(Math.max(cpuStatus.getValue(), memStatus.getValue()));
    }
}
