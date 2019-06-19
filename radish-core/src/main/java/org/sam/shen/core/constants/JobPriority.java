package org.sam.shen.core.constants;

/**
 * Job优先级的枚举类
 * @author clock
 * @date 2019-06-14 10:28
 */
public enum JobPriority {

    ZERO(0),
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9);

    private int value;

    JobPriority(int value) {
        this.value = value;
    }

    int value() {
        return value;
    }
}
