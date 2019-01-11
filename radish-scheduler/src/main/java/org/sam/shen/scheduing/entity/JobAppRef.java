package org.sam.shen.scheduing.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author clock
 * @date 2019/1/7 下午5:56
 */
@Getter
@Setter
public class JobAppRef implements Serializable {

    private static final long serialVersionUID = 1707342671431934677L;

    public JobAppRef(String jobId, String appId) {
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.jobId = jobId;
        this.appId = appId;
    }

    private String id;

    private String jobId;

    private String appId;
}
