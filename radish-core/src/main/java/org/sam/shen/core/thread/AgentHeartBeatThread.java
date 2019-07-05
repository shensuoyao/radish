package org.sam.shen.core.thread;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.agent.RadishAgent;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.model.Monitor;
import org.sam.shen.core.model.Resp;
import org.sam.shen.core.rpc.RestRequest;
import org.sam.shen.core.util.ScriptUtil;

import com.alibaba.fastjson.JSON;

/**
 * @author suoyao
 * @date 2018年8月1日 下午2:58:02
 * Agent 客户端心跳执行线程
 */
@Slf4j
public class AgentHeartBeatThread {

    private static AgentHeartBeatThread instance = new AgentHeartBeatThread();

    public static AgentHeartBeatThread getInstance() {
        return instance;
    }

    /**
     * 心跳线程
     */
    private Thread beatThread;

    private volatile boolean toStop = false;

    public void start(final String rpcUrl, Long agentId, String agentName) {
        //必须设置客户端名称, 否则无法判断是哪一台机器的心跳
        if (StringUtils.isEmpty(agentName)) {
            log.warn(">>>>>>>>>>> radish, agent heartbeat fail, agentName is null.");
        }

        boolean isMonitor;
        Monitor monitor = new Monitor(agentId, agentName);
        // 检测有没有安装sysstat插件，没有则安装
        isMonitor = monitor.getAgentMonitorInfo().getOsName().contains("Linux") && checkMonitorEnv();

        beatThread = new Thread(() -> {
            // HeartBeat
            while (!toStop) {
                try {
                    Resp<Object> resp;
                    if (isMonitor) {
                        resp = RestRequest.post(rpcUrl, monitor.collect(Monitor.MonitorType.All));
                    } else {
                        resp = RestRequest.post(rpcUrl, monitor.getAgentMonitorInfo());
                    }
                    if (Resp.SUCCESS.getCode() != resp.getCode()) {
                        log.error(JSON.toJSONString(resp));
                        log.error("HeartBeat failed the reason is: {}, detail: {}", resp.getMsg(), resp.getData());
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(RadishAgent.getHeartBeat());
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }
            // 心跳停止, Agent客户端下线, 暂停, 停止
            // 销毁资源
            // TODO
        });
        beatThread.setDaemon(true);
        beatThread.start();
    }

    public boolean isStop() {
        return toStop;
    }

    /**
     * @author suoyao
     * @date 下午3:07:53
     * 停止心跳
     */
    public void toStop() {
        toStop = true;
        // interrupt and wait
        beatThread.interrupt();
        try {
            beatThread.join();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * check monitor environment
     * @author clock
     * @date 2018/12/7 上午10:40
     * @return execute result
     */
    private boolean checkMonitorEnv() {
        String checkFileName = RadishAgent.getShPath().concat(File.separator).concat(Constant.CHECK_SHELL_SCRIPT_NAME);
        File file = new File(checkFileName);
        URL fileUrl = this.getClass().getResource(Constant.SHELL_SCRIPT_PATH.concat(File.separator).concat(Constant.CHECK_SHELL_SCRIPT_NAME));
        try {
            ScriptUtil.createAndAuthShellScript(file, fileUrl.openStream());

            // execute script file
            String result = ScriptUtil.execShellWithResult(checkFileName);
            log.info("Monitor check execute result: {}", result);
            return result.trim().endsWith("1");
        } catch (IOException e) {
            log.error("Make monitor check shell file failed. [{}]", e.getMessage());
            return false;
        } finally {
            // delete monitor check shell file.
            try {
                FileUtils.forceDelete(file);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
