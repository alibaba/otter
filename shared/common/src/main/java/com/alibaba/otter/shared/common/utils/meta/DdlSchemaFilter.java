package com.alibaba.otter.shared.common.utils.meta;

/**
 * @author hatterjiang
 */
public interface DdlSchemaFilter {

    boolean accept(String schemaName);
}
