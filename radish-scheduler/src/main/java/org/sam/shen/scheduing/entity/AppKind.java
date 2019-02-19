package org.sam.shen.scheduing.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author clock
 * @date 2019/2/18 下午1:38
 */
@Getter
@Setter
public class AppKind implements Serializable {

    private static final long serialVersionUID = -1602656594700856207L;

    public AppKind(String appId, String kind) {
        this.appId = appId;
        this.kind = kind;
    }

    public AppKind(String id, String appId, String kind) {
        this(appId, kind);
        this.id = id;
    }

    private String id;

    private String appId;

    private String kind;
}
