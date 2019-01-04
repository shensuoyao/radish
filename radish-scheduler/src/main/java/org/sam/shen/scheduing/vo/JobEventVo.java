package org.sam.shen.scheduing.vo;

import lombok.Getter;
import lombok.Setter;
import org.sam.shen.scheduing.entity.JobEvent;
import org.sam.shen.scheduing.entity.JobInfo;

/**
 * @author clock
 * @date 2019/1/3 下午3:52
 */
@Getter
@Setter
public class JobEventVo {

    private JobEvent jobEvent;

    private JobInfo jobInfo;

}
