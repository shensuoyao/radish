package org.sam.shen.scheduing.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.sam.shen.core.model.Resp;
import org.sam.shen.scheduing.entity.AppInfo;
import org.sam.shen.scheduing.mapper.AppInfoMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author clock
 * @date 2019/1/14 下午2:29
 */
@Aspect
@Component
public class ApiAspect {

    @Resource
    private AppInfoMapper appInfoMapper;

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

}
