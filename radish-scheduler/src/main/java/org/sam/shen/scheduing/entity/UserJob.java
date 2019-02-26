package org.sam.shen.scheduing.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author clock
 * @date 2019/2/25 上午10:41
 */
@Getter
@Setter
public class UserJob implements Serializable {

    private static final long serialVersionUID = -4775620931122641714L;

    private Long id;

    private Long userId;

    private Long jobId;

}
