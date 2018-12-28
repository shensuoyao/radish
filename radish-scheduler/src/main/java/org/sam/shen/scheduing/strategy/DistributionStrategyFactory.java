package org.sam.shen.scheduing.strategy;

import org.sam.shen.core.constants.DistributionType;
import org.sam.shen.scheduing.strategy.impl.DateDistributionStrategy;
import org.sam.shen.scheduing.strategy.impl.EnumDistributionStrategy;
import org.sam.shen.scheduing.strategy.impl.ModDistributionStrategy;
import org.sam.shen.scheduing.strategy.impl.PageDistributionStrategy;

/**
 * distribution strategy factory
 * @author clock
 * @date 2018/12/28 上午9:18
 */
public class DistributionStrategyFactory {

    public static DistributionStrategy newInstance(DistributionType type) {
        switch (type) {
            case ENUM:
                return new EnumDistributionStrategy();
            case PAGE:
                return new PageDistributionStrategy();
            case MOD:
                return new ModDistributionStrategy();
            case DATE:
                return new DateDistributionStrategy();
        }
        return null;
    }

}
