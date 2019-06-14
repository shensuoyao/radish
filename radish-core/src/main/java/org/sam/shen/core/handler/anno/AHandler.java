package org.sam.shen.core.handler.anno;

import org.sam.shen.core.constants.HandlerFailStrategy;
import org.sam.shen.core.constants.HandlerType;
import org.sam.shen.core.constants.JobPriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author suoyao
 * 用来执行Job 任务
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface AHandler {
    String name();
    String description() default "";
    boolean enableJob() default false;
    HandlerType handlerType() default HandlerType.H_JAVA;
    String crontab() default "";
    HandlerFailStrategy failStratgy() default HandlerFailStrategy.DISCARD;
    JobPriority priority() default JobPriority.ZERO;
    String expired() default "1d";
}
