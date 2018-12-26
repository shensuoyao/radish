package org.sam.shen.core.http;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.log.LogReader;
import org.sam.shen.core.log.RadishLogFileAppender;
import org.sam.shen.core.model.Resp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author clock
 * @date 2018/12/20 下午2:27
 */
public class HandlerLogServlet extends HttpServlet {

    private static final long serialVersionUID = -1311459296500533699L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String eventId = req.getParameter("eventId");
        Integer beginLineNum = null;
        String logFileName;
        if (StringUtils.isNotEmpty(req.getParameter("beginLineNum"))) {
            beginLineNum = Integer.parseInt(req.getParameter("beginLineNum"));
        }
        // 如果存在logPath，则直接读取该日志文件
        if (StringUtils.isNotEmpty(req.getParameter("logPath"))) {
            logFileName = req.getParameter("logPath");
        } else {
            logFileName = RadishLogFileAppender.makeLogFile(eventId);
        }
        Resp<LogReader> logReaderResp = new Resp<>(RadishLogFileAppender.readLog(logFileName, beginLineNum));

        resp.setContentType("application/json;charset=utf-8");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter printWriter = resp.getWriter();
        printWriter.write(JSON.toJSONString(logReaderResp));
        printWriter.close();
    }
}
