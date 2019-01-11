package org.sam.shen.scheduing.vo;

import lombok.Getter;
import lombok.Setter;
import org.sam.shen.scheduing.entity.JobInfo;

/**
 * @author clock
 * @date 2019/1/8 下午4:46
 */
@Getter
@Setter
public class JobApiVo extends JobInfo {

    private static final long serialVersionUID = 692519794876610682L;

    private String refId;

    private String appId;

}
