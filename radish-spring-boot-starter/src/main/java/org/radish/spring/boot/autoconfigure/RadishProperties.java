package org.radish.spring.boot.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author clock
 * @date 2018/12/18 下午3:51
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "radish")
public class RadishProperties {

    private Agent agent;

    private Scheduler scheduler;

    /**
     * 读取客户端日志的访问模式
     */
    private String logViewMode = "servlet";

//    private LogViewServlet logViewServlet;

    private LogViewNetty logViewNetty;

    @Getter
    @Setter
    public static class Agent {
        /**
         * 客户端名称
         */
        private String name;

        /**
         * 客户端IP
         */
        private String ip;

        /**
         * 客户端端口
         */
        private Integer port;

        /**
         * 客户端存放日志的目录
         */
        private String logpath;

        /**
         * 客户端存放脚本的目录
         */
        private String shpath;
    }

    @Getter
    @Setter
    public static class Scheduler {
        /**
         * 调度中心的连接地址
         */
        private String server;
    }

//    @Getter
//    @Setter
//    public static class LogViewServlet {}

    @Getter
    @Setter
    public static class LogViewNetty {
        private Integer port;
    }
}
