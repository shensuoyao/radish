package org.sam.shen.core.constants;

/**
 * @author clock
 * @date 2018/12/27 下午3:33
 */
public enum DistributionType {

    ENUM("enum"),
    PAGE("page"),
    MOD("mod"),
    DATE("date");

    private String value;

    DistributionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
