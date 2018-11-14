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
    @JSONField(name = "cpu.util.user")
    private String cpuUser;

    /**
     * 系统级别运行CPU时间占比
     */
    @JSONField(name = "cpu.util.system")
    private String cpuSystem;

    /**
     * 用户级别执行nice优先级操作CPU时间占比
     */
    @JSONField(name = "cpu.util.nice")
    private String cpuNice;

    /**
     * IO等待时间占CPU运行时间的百分比
     */
    @JSONField(name = "cpu.util.iowait")
    private String cpuIowait;

    /**
     * 管理程序为另一个虚拟机进程提供服务等待虚拟CPU的时间占CPU总时间的百分比
     */
    @JSONField(name = "cpu.util.steal")
    private String cpuSteal;

    /**
     * 空闲时间占CPU运行总时间的百分比
     */
    @JSONField(name = "cpu.util.idle")
    private String cpuIdle;

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
    private String memoryUsedUtil;

    /**
     * 保证内存不溢出所需内存的百分比
     */
    @JSONField(name = "mem.commit")
    private String memoryCommitUtil;

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
}
