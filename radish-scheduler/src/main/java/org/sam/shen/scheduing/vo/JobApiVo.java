package org.sam.shen.scheduing.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sam.shen.scheduing.entity.JobInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author clock
 * @date 2019/1/8 下午4:46
 */
@Slf4j
@Getter
@Setter
public class JobApiVo extends JobInfo {

    private static final long serialVersionUID = 692519794876610682L;

    private String refId;

    private String appId;

    /**
     * 外部对象非空属性覆盖当前对象
     */
    public void update(JobApiVo job) {
        Class<?> clazz = this.getClass().getSuperclass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            // 去掉
            if ("serialVersionUID".equals(field.getName())) {
                continue;
            }
            try {
                String fieldName = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
                Method getMethod = clazz.getDeclaredMethod("get" + fieldName);
                Method setMethod = clazz.getDeclaredMethod("set" + fieldName, field.getType());
                Object value = getMethod.invoke(job);
                if (value != null && (!(value instanceof String) || !"".equals(value.toString()))) {
                    setMethod.invoke(this, value);
                }
            } catch (Exception e) {
                log.debug("{}[{}] occurred error", clazz.getSimpleName(), field.getName());
            }
        }
    }

}
