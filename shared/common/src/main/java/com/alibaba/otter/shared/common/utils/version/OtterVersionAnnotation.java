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

package com.alibaba.otter.shared.common.utils.version;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A package attribute that captures the version of Otter that was compiled.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface OtterVersionAnnotation {

    /**
     * Get the Otter version
     * 
     * @return the version string "3.0.5-r"
     */
    String version();

    /**
     * Get the username that compiled Otter.
     */
    String user();

    /**
     * Get the date when Otter was compiled.
     * 
     * @return the date in unix 'date' format
     */
    String date();

    /**
     * Get the url for the subversion repository.
     */
    String url();

    /**
     * Get the subversion revision.
     * 
     * @return the revision number as a string (eg. "168168")
     */
    String revision();

    /**
     * Get the branch from which this was compiled.
     * 
     * @return The branch name, e.g. "trunk" or "branches/branch-3.0.5"
     */
    String branch();

    /**
     * Get a checksum of the source files from which Otter was compiled.
     * 
     * @return a string that uniquely identifies the source
     */
    String srcChecksum();
}
