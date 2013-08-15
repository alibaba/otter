package com.alibaba.otter.shared.common.utils.meta;

/**
 * @author hatterjiang
 */
public interface DdlTableNameFilter {

    boolean accept(String catalogName, String schemaName, String tableName);
}
