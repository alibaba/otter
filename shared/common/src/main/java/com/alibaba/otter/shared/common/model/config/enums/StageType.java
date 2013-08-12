package com.alibaba.otter.shared.common.model.config.enums;

/**
 * S.E.T.L的阶段类型
 * 
 * @author jianghang 2011-9-8 下午12:40:29
 */
public enum StageType {

    SELECT, EXTRACT, TRANSFORM, LOAD;

    public boolean isSelect() {
        return this.equals(StageType.SELECT);
    }

    public boolean isExtract() {
        return this.equals(StageType.EXTRACT);
    }

    /**
     * transform和load一定会同时出现
     */
    public boolean isTransform() {
        return this.equals(StageType.TRANSFORM);
    }

    /**
     * transform和load一定会同时出现
     */
    public boolean isLoad() {
        return this.equals(StageType.LOAD);
    }
}
