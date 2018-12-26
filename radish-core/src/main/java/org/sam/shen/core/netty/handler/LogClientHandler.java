package org.sam.shen.core.netty.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import org.sam.shen.core.log.LogReader;
import org.sam.shen.core.model.Resp;

/**
 * @author clock
 * @date 2018/12/21 上午9:14
 */
public class LogClientHandler extends SimpleChannelInboundHandler {

    @Getter
    private Resp<LogReader> resp;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg != null) {
            resp = JSON.parseObject(msg.toString(), new TypeReference<Resp<LogReader>>() {});
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 读取消息完毕，关闭连接
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
