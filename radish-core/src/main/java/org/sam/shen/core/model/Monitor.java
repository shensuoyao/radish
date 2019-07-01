package org.sam.shen.core.model;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.agent.RadishAgent;
import org.sam.shen.core.util.IpUtil;
import org.sam.shen.core.util.ScriptUtil;
import org.sam.shen.core.util.SystemUtil;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author clock
 * @date 2018/10/30 下午5:59
 */
@Slf4j
public class Monitor {

    @Getter
    public enum MonitorType {
        All("CMINPJS"), CPU("C"), Memory("M"), IO("I"), NETWORK("N"), PHP("P"), JAVA("J"), SYSTEM("S");

        private String name;

        MonitorType(String name) {
            this.name = name;
        }
    }

    @Getter
    private AgentMonitorInfo agentMonitorInfo;

    public Monitor() {
        this.agentMonitorInfo = new AgentMonitorInfo();
        agentMonitorInfo.setCpuCount(SystemUtil.cpuCount());
        if (StringUtils.isEmpty(agentMonitorInfo.getIp())) {
            agentMonitorInfo.setIp(IpUtil.getIp());
        }
        agentMonitorInfo.setOsName(SystemUtil.osName());
        agentMonitorInfo.setOsVersion(SystemUtil.osVersion());
        agentMonitorInfo.setAgentName(IpUtil.getHostName());
        // agent设置默认运行状态
        agentMonitorInfo.setAgentStatus(AgentMonitorInfo.AgentStatus.NULL);
    }

    public Monitor(Long agentId) {
        this();
        agentMonitorInfo.setAgentId(agentId);
    }

    public Monitor(Long agentId, String agentName) {
        this(agentId);
        agentMonitorInfo.setAgentName(agentName);
    }


    /**
     * 采集服务器的监控信息
     * @author clock
     * @date 2018/11/6 上午10:29
     * @param monitorType 监控内容
     * @return 监控信息
     */
    public AgentMonitorInfo collect(MonitorType... monitorType) {
        String param = Arrays.stream(monitorType).map(MonitorType::getName).collect(Collectors.joining(""));
        String result = ScriptUtil.execShellWithResult(RadishAgent.getShFilePath(), param);

        Map<String, Object> map = JSON.parseObject(JSON.toJSONString(agentMonitorInfo));
        List<Map<String, Object>> javaList = new ArrayList<>();
        Map<String, Map<String, Object>> netMap = new HashMap<>();
        BufferedReader br = new BufferedReader(new StringReader(result));
        try {
            String line;
            while (StringUtils.isNotEmpty(line = br.readLine().trim())) {
                String[] kv = line.split(":");
                if (kv.length == 2 && StringUtils.isNotEmpty(kv[0]) && StringUtils.isNotEmpty(kv[1])) {
                    if (kv[0].startsWith("java")) { // java服务占用内存一对多关系，特殊处理
                        Map<String, Object> jMap = new HashMap<>();
                        jMap.put("name", kv[0].split("\\.")[1]);
                        jMap.put("rss", kv[1]);
                        javaList.add(jMap);
                    } else if (kv[0].startsWith("network")) { // 网卡一对多关系，特殊处理
                        String iface = kv[0].split("\\.")[1];
                        if (netMap.get(iface) == null) {
                            Map<String, Object> nMap = new HashMap<>();
                            nMap.put("iface", iface);
                            nMap.put(kv[0].split("\\.")[2], kv[1]);
                            netMap.put(iface, nMap);
                        } else {
                            netMap.get(iface).put(kv[0].split("\\.")[2], kv[1]);
                        }
                    } else {
                        map.put(kv[0], kv[1]);
                    }
                }
            }
        } catch (Exception e) {
            log.error("error:", e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
            	log.error("error:", e);
            }
        }
        map.put("javaMemoryList", javaList);
        map.put("networkIOList", netMap.values());
        agentMonitorInfo = JSON.parseObject(JSON.toJSONString(map), AgentMonitorInfo.class);
        agentMonitorInfo.computeAgentStatus();
        return agentMonitorInfo;
    }
}
