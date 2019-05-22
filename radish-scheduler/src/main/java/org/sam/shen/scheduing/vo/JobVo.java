package org.sam.shen.scheduing.vo;

import org.sam.shen.scheduing.entity.JobInfo;

/**
 * @author clock
 * @date 2019-05-22 15:20
 */
public class JobVo extends JobInfo {

    private String expiredUnit;

    public String getExpiredUnit() {
        return expiredUnit;
    }

    public void setExpiredUnit(String expiredUnit) {
        this.expiredUnit = expiredUnit;
    }
}
