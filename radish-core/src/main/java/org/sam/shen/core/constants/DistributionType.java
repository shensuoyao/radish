package org.sam.shen.core.constants;

/**
 * @author clock
 * @date 2018/12/27 下午3:33
 */
public enum DistributionType {

    ENUM("枚举"),
    PAGE("分页"),
    MOD("取模"),
    DATE("时间");

    private String name;

    DistributionType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
