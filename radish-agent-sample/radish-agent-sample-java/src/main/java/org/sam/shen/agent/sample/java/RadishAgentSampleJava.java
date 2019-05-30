package org.sam.shen.agent.sample.java;

import org.sam.shen.core.agent.RadishAgent;
import org.sam.shen.core.netty.HandlerLogNettyServer;

/**
 * @author clock
 * @date 2019-05-30 13:31
 */
public class RadishAgentSampleJava {

    public static void main(String[] args) {
        // 初始化agent监听程序
        RadishAgent radishAgent = new RadishAgent();
        radishAgent.getAgentInfo().setAgentName("agent_java"); // 可以自己设置，默认是agent_[ip]
        radishAgent.setScheduingServer("http://127.0.0.1:8888/radish-scheduing");
        radishAgent.start();

        // 由于是java启动，没有web容器，因此需要启动netty监听程序，用于server读取agent日志
        HandlerLogNettyServer.getInstance(radishAgent.getAgentInfo().getNettyPort()).start();
    }

}
