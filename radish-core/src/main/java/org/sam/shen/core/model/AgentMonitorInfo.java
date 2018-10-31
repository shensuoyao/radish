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
public class AgentMonitorInfo implements Serializable {

    private static final long serialVersionUID = -3919488232847843771L;

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
     * 用户级别运行CPU时间占比
     */
    private String cpuUser;

    /**
     * 系统级别运行CPU时间占比
     */
    private String cpuSystem;

    /**
     * 用户级别执行nice优先级操作CPU时间占比
     */
    private String cpuNice;

    /**
     * IO等待时间占CPU运行时间的百分比
     */
    private String cpuIowait;

    /**
     * 管理程序为另一个虚拟机进程提供服务等待虚拟CPU的时间占CPU总时间的百分比
     */
    private String cpuSteal;

    /**
     * 空闲时间占CPU运行总时间的百分比
     */
    private String cpuIdle;

    /**
     * 空闲内存
     */
    private Long memoryFree;

    /**
     * 已使用内存
     */
    private Long memoryUsed;

    /**
     * 缓冲区所使用的内存
     */
    private Long memoryBuffers;

    /**
     * 缓存所使用的内存
     */
    private Long memoryCached;

    /**
     * 保证内存不溢出所需的内存
     */
    private Long memoryCommit;

    /**
     * 已用内存的百分比
     */
    private String memoryUsedUtil;

    /**
     * 保证内存不溢出所需内存的百分比
     */
    private String memoryCommitUtil;

    /**
     * 磁盘每秒的传输总次数
     */
    private Double ioTps;

    /**
     * 磁盘每秒的写入请求数
     */
    private Double ioWtps;

    /**
     * 磁盘每秒的读取请求数
     */
    private Double ioRtps;

    /**
     * 近1分钟的系统平均负载
     */
    private Double ldavg1;

    /**
     * 近5分钟的系统平均负载
     */
    private Double ldavg5;

    /**
     * 近15分钟的系统平均负载
     */
    private Double ldavg15;

    /**
     * php进程所占的物理内存
     */
    private Long phpRss;

    /**
     * 各个java服务占用的物理内存
     */
    private List<JavaMemory> javaMemoryList = new ArrayList<>();

    /**
     * 各网卡的io情况
     */
    private List<NetworkIO> networkIOList = new ArrayList<>();

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    @JSONField(name = "cpu.util.user")
    public void setCpuUser(String cpuUser) {
        this.cpuUser = cpuUser;
    }

    @JSONField(name = "cpu.util.system")
    public void setCpuSystem(String cpuSystem) {
        this.cpuSystem = cpuSystem;
    }

    @JSONField(name = "cpu.util.nice")
    public void setCpuNice(String cpuNice) {
        this.cpuNice = cpuNice;
    }

    @JSONField(name = "cpu.util.iowait")
    public void setCpuIowait(String cpuIowait) {
        this.cpuIowait = cpuIowait;
    }

    @JSONField(name = "cpu.util.steal")
    public void setCpuSteal(String cpuSteal) {
        this.cpuSteal = cpuSteal;
    }

    @JSONField(name = "cpu.util.idle")
    public void setCpuIdle(String cpuIdle) {
        this.cpuIdle = cpuIdle;
    }

    @JSONField(name = "mem.kbmemfree")
    public void setMemoryFree(Long memoryFree) {
        this.memoryFree = memoryFree;
    }

    @JSONField(name = "mem.kbmemused")
    public void setMemoryUsed(Long memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    @JSONField(name = "mem.kbmembuffers")
    public void setMemoryBuffers(Long memoryBuffers) {
        this.memoryBuffers = memoryBuffers;
    }

    @JSONField(name = "mem.kbmemcached")
    public void setMemoryCached(Long memoryCached) {
        this.memoryCached = memoryCached;
    }

    @JSONField(name = "mem.kbmemcommit")
    public void setMemoryCommit(Long memoryCommit) {
        this.memoryCommit = memoryCommit;
    }

    @JSONField(name = "mem.memused")
    public void setMemoryUsedUtil(String memoryUsedUtil) {
        this.memoryUsedUtil = memoryUsedUtil;
    }

    @JSONField(name = "mem.commit")
    public void setMemoryCommitUtil(String memoryCommitUtil) {
        this.memoryCommitUtil = memoryCommitUtil;
    }

    @JSONField(name = "io.tps")
    public void setIoTps(Double ioTps) {
        this.ioTps = ioTps;
    }

    @JSONField(name = "io.wtps")
    public void setIoWtps(Double ioWtps) {
        this.ioWtps = ioWtps;
    }

    @JSONField(name = "io.rtps")
    public void setIoRtps(Double ioRtps) {
        this.ioRtps = ioRtps;
    }

    @JSONField(name = "system.ldavg-1")
    public void setLdavg1(Double ldavg1) {
        this.ldavg1 = ldavg1;
    }

    @JSONField(name = "system.ldavg-5")
    public void setLdavg5(Double ldavg5) {
        this.ldavg5 = ldavg5;
    }

    @JSONField(name = "system.ldavg-15")
    public void setLdavg15(Double ldavg15) {
        this.ldavg15 = ldavg15;
    }

    @JSONField(name = "php.rss")
    public void setPhpRss(Long phpRss) {
        this.phpRss = phpRss;
    }

    public void setJavaMemoryList(List<JavaMemory> javaMemoryList) {
        this.javaMemoryList = javaMemoryList;
    }

    public void setNetworkIOList(List<NetworkIO> networkIOList) {
        this.networkIOList = networkIOList;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    @Getter
    @Setter
    static class JavaMemory implements Serializable {
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
    static class NetworkIO implements Serializable {
        private static final long serialVersionUID = -5244193663281737881L;

        /**
         * 网卡接口
         */
        private String iface;
        /**
         * 每秒接受的数据量，KB
         */
        private Double rx;
        /**
         * 每秒传输的数据量，KB
         */
        private Double tx;

        public void setIface(String iface) {
            this.iface = iface;
        }

        @JSONField(name = "rxkB")
        public void setRx(Double rx) {
            this.rx = rx;
        }

        @JSONField(name = "txkB")
        public void setTx(Double tx) {
            this.tx = tx;
        }
    }
}
