package org.sam.shen.core.netty.channel;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.sam.shen.core.log.LogReader;
import org.sam.shen.core.model.Resp;
import org.sam.shen.core.netty.handler.LogClientHandler;

import java.util.concurrent.CountDownLatch;

/**
 * @author clock
 * @date 2018/12/21 上午9:10
 */
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private LogClientHandler handler;

    private CountDownLatch countDownLatch;

    public ClientChannelInitializer(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        handler = new LogClientHandler(countDownLatch);
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("decode", new StringDecoder());
        pipeline.addLast("encode", new StringEncoder());
        pipeline.addLast("handler", handler);
    }

    public Resp<LogReader> getMessage() {
        return handler.getResp();
    }

}
