package org.sam.shen.core.annotations;

import org.sam.shen.core.constants.MonitorType;

import java.lang.annotation.*;

/**
 * @author clock
 * @date 2019-05-10 18:04
 */
@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RadishLog {

    MonitorType monitorType();

    long timeout() default -1;

}
