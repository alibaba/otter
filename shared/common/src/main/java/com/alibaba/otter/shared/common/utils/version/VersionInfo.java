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

import org.apache.commons.lang.SystemUtils;

/**
 * This class finds the package info for Otter and the OtterVersionAnnotation information.
 */
public class VersionInfo {

    private static Package                myPackage;
    private static OtterVersionAnnotation version;

    static {
        myPackage = OtterVersionAnnotation.class.getPackage();
        version = myPackage.getAnnotation(OtterVersionAnnotation.class);
    }

    /**
     * Get the meta-data for the Otter package.
     * 
     * @return
     */
    static Package getPackage() {
        return myPackage;
    }

    /**
     * Get the Otter version.
     * 
     * @return the Otter version string, eg. "3.0.5-r"
     */
    public static String getVersion() {
        return (version != null) ? version.version() : "Unknown";
    }

    /**
     * Get the subversion revision number for the root directory
     * 
     * @return the revision number, eg. "168168"
     */
    public static String getRevision() {
        return (version != null) ? version.revision() : "Unknown";
    }

    /**
     * Get the branch on which this originated.
     * 
     * @return The branch name, e.g. "trunk" or "branches/branch-3.0.5"
     */
    public static String getBranch() {
        return (version != null) ? version.branch() : "Unknown";
    }

    /**
     * The date that Otter was compiled.
     * 
     * @return the compilation date in unix date format
     */
    public static String getDate() {
        return (version != null) ? version.date() : "Unknown";
    }

    /**
     * The user that compiled Otter.
     * 
     * @return the username of the user
     */
    public static String getUser() {
        return (version != null) ? version.user() : "Unknown";
    }

    /**
     * Get the subversion URL for the root Otter directory.
     */
    public static String getUrl() {
        return (version != null) ? version.url() : "Unknown";
    }

    /**
     * Get the checksum of the source files from which Otter was built.
     */
    public static String getSrcChecksum() {
        return (version != null) ? version.srcChecksum() : "Unknown";
    }

    /**
     * Returns the buildVersion which includes version, revision, user and date.
     */
    public static String getBuildVersion() {
        StringBuilder buf = new StringBuilder();

        buf.append(SystemUtils.LINE_SEPARATOR);
        buf.append("[OTTER Version Info]").append(SystemUtils.LINE_SEPARATOR);
        buf.append("[version ]").append(VersionInfo.getVersion()).append(SystemUtils.LINE_SEPARATOR);
        buf.append("[revision]").append(VersionInfo.getRevision()).append(SystemUtils.LINE_SEPARATOR);
        buf.append("[compiler]").append(VersionInfo.getUser()).append(SystemUtils.LINE_SEPARATOR);
        buf.append("[date    ]").append(VersionInfo.getDate()).append(SystemUtils.LINE_SEPARATOR);
        buf.append("[checksum]").append(VersionInfo.getSrcChecksum()).append(SystemUtils.LINE_SEPARATOR);
        buf.append("[branch  ]").append(VersionInfo.getBranch()).append(SystemUtils.LINE_SEPARATOR);
        buf.append("[url     ]").append(VersionInfo.getUrl()).append(SystemUtils.LINE_SEPARATOR);

        return buf.toString();
    }

    public static void main(String[] args) {
        System.out.println(getBuildVersion());
    }
}
