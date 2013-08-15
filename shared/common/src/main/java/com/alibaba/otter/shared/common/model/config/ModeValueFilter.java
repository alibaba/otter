package com.alibaba.otter.shared.common.model.config;

/**
 * @author hatterjiang
 */
public interface ModeValueFilter {

    boolean accept(final String value);
}
