/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.shared.common.utils.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * An enumeration wrapper around JDBC table types.
 */
public enum TableType {

    /**
     * Unknown
     */
    unknown,

    /**
     * System table
     */
    system_table,

    /**
     * Global temporary
     */
    global_temporary,

    /**
     * Local temporary
     */
    local_temporary,

    /**
     * Table
     */
    table,

    /**
     * View
     */
    view,

    /**
     * Alias
     */
    alias,

    /**
     * Synonym
     */
    synonym, ;

    /**
     * Converts an array of table types to an array of their corresponding string values.
     * 
     * @param tableTypes Array of table types
     * @return Array of string table types
     */
    public static String[] toStrings(final TableType[] tableTypes) {
        if ((tableTypes == null) || (tableTypes.length == 0)) {
            return new String[0];
        }

        final List<String> tableTypeStrings = new ArrayList<String>(tableTypes.length);

        for (final TableType tableType : tableTypes) {
            if (tableType != null) {
                tableTypeStrings.add(tableType.toString().toUpperCase(Locale.ENGLISH));
            }
        }

        return tableTypeStrings.toArray(new String[tableTypeStrings.size()]);
    }

    /**
     * Converts an array of string table types to an array of their corresponding enumeration values.
     * 
     * @param tableTypeStrings Array of string table types
     * @return Array of table types
     */
    public static TableType[] valueOf(final String[] tableTypeStrings) {
        if ((tableTypeStrings == null) || (tableTypeStrings.length == 0)) {
            return new TableType[0];
        }

        final List<TableType> tableTypes = new ArrayList<TableType>(tableTypeStrings.length);

        for (final String tableTypeString : tableTypeStrings) {
            tableTypes.add(valueOf(tableTypeString.toLowerCase(Locale.ENGLISH)));
        }

        return tableTypes.toArray(new TableType[tableTypes.size()]);
    }
}
