package org.sam.shen.core.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.sam.shen.core.netty.channel.ServerChannelInitializer;


/**
 * @author clock
 * @date 2018/12/20 下午4:18
 */
@Slf4j
public class HandlerLogNettyServer extends Thread {

    private static HandlerLogNettyServer instance;

    private Integer port;

    private HandlerLogNettyServer(Integer port) {
        this.port = port;
    }

    public static HandlerLogNettyServer getInstance(Integer port) {
        if (instance == null) {
            synchronized (HandlerLogNettyServer.class) {
                if (instance == null) {
                    instance = new HandlerLogNettyServer(port);
                }
            }
        }
        return instance;
    }

    @Override
    public void start() {
        log.info(">>>>>>>>>>> 初始化netty server程序，用于服务端读取agent日志");
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ServerChannelInitializer());

            // 绑定监听端口
            ChannelFuture cf = serverBootstrap.bind(port).sync();
            log.info("<<<<<<<<<<< netty server已经初始化完毕，可以接受读取agent日志的请求");

            // 阻塞线程，同步等待netty server关闭
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.info("<<<<<<<<<<< netty server启动异常：{}", e.getMessage());
        } finally {
            // 优雅关闭
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
