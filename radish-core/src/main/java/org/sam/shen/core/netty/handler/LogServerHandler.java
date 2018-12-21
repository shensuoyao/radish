package org.sam.shen.core.netty.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.log.LogReader;
import org.sam.shen.core.log.RadishLogFileAppender;
import org.sam.shen.core.model.Resp;

import java.util.Map;

/**
 * @author clock
 * @date 2018/12/20 下午5:18
 */
@Slf4j
public class LogServerHandler extends SimpleChannelInboundHandler {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        log.info("Log handler receive msg: [{}]", o.toString());
        Map<String, Object> data = JSON.parseObject(o.toString(), new TypeReference<Map<String, Object>>() {});
        String method = data.get("method") == null ? null : data.get("method").toString();

        // 如果
        if ("handler-log".equals(method)) {
            String eventId = data.get("eventId") == null ? null : data.get("eventId").toString();
            String beginLineNum = data.get("beginLineNum") == null ? null : data.get("beginLineNum").toString();
            Integer bln = StringUtils.isEmpty(beginLineNum) ? null : Integer.parseInt(beginLineNum);
            String logFileName = RadishLogFileAppender.makeLogFile(eventId);
            Resp<LogReader> logReaderResp = new Resp<>(RadishLogFileAppender.readLog(logFileName, bln));

            channelHandlerContext.writeAndFlush(JSON.toJSONString(logReaderResp));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }
}
