package org.sam.shen.core.netty;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.sam.shen.core.log.LogReader;
import org.sam.shen.core.model.Resp;
import org.sam.shen.core.netty.channel.ClientChannelInitializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author clock
 * @date 2018/12/21 下午3:06
 */

public class HandlerLogNettyClient {

    private String host;

    private Integer port;

    private Bootstrap bootstrap;

    /**
     * NIO是异步消息，通过CountDownLatch用于同步返回数据
     */
    private CountDownLatch countDownLatch;

    private ClientChannelInitializer initializer;

    public HandlerLogNettyClient(String host, Integer port) {
        this.host = host;
        this.port = port;
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        countDownLatch = new CountDownLatch(1);
        initializer = new ClientChannelInitializer(countDownLatch);
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).handler(initializer);
    }

    public Resp<LogReader> sendMessage(String eventId, Integer beginLineNum) {
        try {
            Channel channel = bootstrap.connect(host, port).sync().channel();
            Map<String, Object> message = new HashMap<>();
            message.put("method", "handler-log");
            message.put("eventId", eventId);
            message.put("beginLineNum", beginLineNum);
            channel.writeAndFlush(JSON.toJSONString(message));
            // 等待agent接受消息并返回，需要设置超时时间，防止一直阻塞线程
            countDownLatch.await(10L, TimeUnit.SECONDS);
            return initializer.getMessage();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
