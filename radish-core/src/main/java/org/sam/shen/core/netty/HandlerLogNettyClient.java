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

import java.util.Map;

/**
 * @author clock
 * @date 2018/12/21 下午3:06
 */

public class HandlerLogNettyClient {

    private String host;

    private Integer port;

    private Bootstrap bootstrap;

    private EventLoopGroup eventLoopGroup;

    private ClientChannelInitializer initializer;

    public HandlerLogNettyClient(String host, Integer port) {
        this.host = host;
        this.port = port;
        eventLoopGroup = new NioEventLoopGroup();
        initializer = new ClientChannelInitializer();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).handler(initializer);
    }

    public Resp<LogReader> sendMessage(Map<String, Object> message) {
        try {
            Channel channel = bootstrap.connect(host, port).sync().channel();
            channel.writeAndFlush(JSON.toJSONString(message));
            // 等待agent接受消息并返回
            channel.closeFuture().sync();
            return initializer.getMessage();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
        return null;
    }
}
