package org.sam.shen.scheduing.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.sam.shen.core.model.Resp;
import org.sam.shen.scheduing.entity.AppInfo;
import org.sam.shen.scheduing.entity.User;
import org.sam.shen.scheduing.mapper.AppInfoMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Objects;

/**
 * @author clock
 * @date 2019/1/14 下午2:29
 */
@Aspect
@Component
public class CommonAspect {

    @Resource
    private AppInfoMapper appInfoMapper;


    /**
     * 外部接口的拦截切面，主要用于一些固定参数的校验
     * @author clock
     * @date 2019/3/12 下午1:26
     * @param joinPoint 切入点
     * @return 返回值
     */
    @Around("execution(* org.sam.shen.scheduing.api.JobApi.*(..))")
    public Object checkParams(ProceedingJoinPoint joinPoint) throws Throwable {
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
            }
            if ("appId".equals(param)) {
                if (arg == null || "".equals(arg.toString())) {
                    return new Resp<>(Resp.FAIL.getCode(), "应用ID不能为空！");
                }
                // 校验应用ID是否存在
                AppInfo appInfo = appInfoMapper.selectAppInfoById(arg.toString());
                if (appInfo == null) {
                    return new Resp<>(Resp.FAIL.getCode(), "无效的应用ID！");
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

}
