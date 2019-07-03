package org.sam.shen.scheduing.aop;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.sam.shen.core.annotations.RadishLog;
import org.sam.shen.core.constants.MonitorType;
import org.sam.shen.core.event.HandlerEvent;
import org.sam.shen.core.model.AgentMonitorInfo;
import org.sam.shen.core.model.MonitorInfo;
import org.sam.shen.core.model.Resp;
import org.sam.shen.core.rpc.RestRequest;
import org.sam.shen.scheduing.entity.AppInfo;
import org.sam.shen.scheduing.entity.JobEvent;
import org.sam.shen.scheduing.entity.User;
import org.sam.shen.scheduing.mapper.AppInfoMapper;
import org.sam.shen.scheduing.vo.JobApiVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author clock
 * @date 2019/1/14 下午2:29
 */
@Slf4j
@Aspect
@Component
public class CommonAspect {

    @Resource
    private AppInfoMapper appInfoMapper;

    @Value("${monitor.url}")
    private String url;

    @Value("${monitor.heartbeat.timeout}")
    private long beatTimeout;

    @Value("${monitor.event.timeout}")
    private long eventTimeout;

    @Value("${monitor.alarmType}")
    private String alarmType;

    @Value("${monitor.enable}")
    private boolean enable;


    /**
     * 外部接口的拦截切面，主要用于一些固定参数的校验
     * @author clock
     * @date 2019/3/12 下午1:26
     * @param joinPoint 切入点
     * @return 返回值
     */
    @Around("execution(* org.sam.shen.scheduing.api.JobApi.*(..))")
    public Object checkParams(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String appId = request.getHeader("appId");
        if (StringUtils.isEmpty(appId)) {
            return new Resp<>(Resp.FAIL.getCode(), "应用ID不能为空！");
        }
        // 校验应用ID是否存在
        AppInfo appInfo = appInfoMapper.selectAppInfoById(appId);
        if (appInfo == null) {
            return new Resp<>(Resp.FAIL.getCode(), "无效的应用ID！");
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] params = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            Object arg = args[i];
            if ("jobId".equals(param)) {
                if (arg == null) {
                    return new Resp<>(Resp.FAIL.getCode(), "任务ID不能为空！");
                }
            } else if ("jobApiVo".equals(param)) {
                if (arg != null) {
                    ((JobApiVo) arg).setUserId(appInfo.getUserId());
                }
            }
        }
        return joinPoint.proceed();
    }

    /**
     * 登陆权限校验切面
     * @author clock
     * @date 2019/3/12 下午1:28
     * @param joinPoint 切入点
     * @return 返回值
     */
    @Around("execution(* org.sam.shen.scheduing.controller.portal.*.*(..)) && !execution(* org.sam.shen.scheduing.controller.portal.LoginController.login*(..))")
    public Object checkSession(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath());
            return null;
        } else {
            return joinPoint.proceed();
        }
    }

    /**
     * 监控日志切点
     * @author clock
     * @date 2019-05-16 17:29
     * @param joinPoint 切入点
     */
    @AfterReturning(value = "@annotation(org.sam.shen.core.annotations.RadishLog)", returning = "result")
    public void handleRadishLog(JoinPoint joinPoint, Object result) {
        if (!enable) {
            return;
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取注解信息
        RadishLog radishLog = signature.getMethod().getAnnotation(RadishLog.class);
        MonitorType monitorType = radishLog.monitorType();
        long timeout = radishLog.timeout();
        // 生成监控信息
        MonitorInfo monitorInfo = new MonitorInfo();
        monitorInfo.setCreateTime(new Date());
        monitorInfo.setMonitorType(monitorType);
        monitorInfo.setAlarmType(alarmType);
        switch (monitorType) {
            case HEARTBEAT:
                AgentMonitorInfo object = (AgentMonitorInfo) joinPoint.getArgs()[0];
                monitorInfo.setBizId(Long.toString(object.getAgentId()));
                if (timeout == -1L) {
                    timeout = beatTimeout;
                }
                monitorInfo.setExtra(Collections.singletonMap("timeout", Long.toString(timeout)));
                break;
            case EVENT:
                if (timeout == -1L) {
                    timeout = eventTimeout;
                }
                Map<String, String> extra = new HashMap<>();
                extra.put("timeout", Long.toString(timeout));
                String methodName = signature.getMethod().getName();
                if (methodName.contains("handlerEventReport")) {
                    HandlerEvent event = (HandlerEvent) joinPoint.getArgs()[0];
                    monitorInfo.setBizId(event.getEventId());
                    extra.put("step", "3");
                } else if (methodName.contains("batchInsert")) {
                    List<JobEvent> list = (List<JobEvent>) joinPoint.getArgs()[0];
                    String bizId = list.stream().map(JobEvent::getEventId).collect(Collectors.joining(","));
                    monitorInfo.setBizId(bizId);
                    extra.put("step", "1");
                } else if (methodName.contains("triggerEvent")) {
                    Resp<List<HandlerEvent>> resp = (Resp<List<HandlerEvent>>) result;
                    String bizId = resp.getData().stream().map(HandlerEvent::getEventId).collect(Collectors.joining(","));
                    monitorInfo.setBizId(bizId);
                    extra.put("step", "2");
                }
                monitorInfo.setExtra(extra);
                break;
        }
        // 发送到监控中心
        try {
            RestRequest.post(url, monitorInfo);
        } catch (Exception e) {
            log.error("Request monitor failed.[{}]", e.getMessage());
        }
    }

}
