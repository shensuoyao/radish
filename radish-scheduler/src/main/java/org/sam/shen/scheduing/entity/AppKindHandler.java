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
public class AppKindHandler implements Serializable {

    private static final long serialVersionUID = 189892745504267221L;

    private String id;

    private String kindId;

    private String handlerId;

    public AppKindHandler(String id, String kindId, String handlerId) {
        this.id = id;
        this.kindId = kindId;
        this.handlerId = handlerId;
    }

}
