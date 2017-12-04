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

package com.alibaba.otter.common.push.datasource.media;

import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.common.push.datasource.DataSourceHanlder;
import com.alibaba.otter.shared.common.model.config.data.DataMediaType;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.google.common.base.Function;
import com.google.common.collect.OtterMigrateMap;

/**
 * media group 的 url 为： jdbc:mysql://groupKey=xxx
 * 
 * @author jianghang 2013-4-18 下午03:33:28
 * @version 4.1.8
 */
public class MediaPushDataSourceHandler implements DataSourceHanlder {

    private static final Logger                       log     = LoggerFactory.getLogger(MediaPushDataSourceHandler.class);

    private static final Pattern                      PATTERN = Pattern.compile("jdbc:mysql://groupKey=([^&/]+).*",
                                                                                Pattern.CASE_INSENSITIVE);

    /**
     * 一个pipeline下面有一组DataSource.<br>
     * key = pipelineId<br>
     * value = key(dataMediaSourceId)-value(DataSource)<br>
     */
    private Map<Long, Map<DbMediaSource, DataSource>> dataSources;

    public MediaPushDataSourceHandler(){
        // 构建第一层map
        dataSources = OtterMigrateMap.makeComputingMap(new Function<Long, Map<DbMediaSource, DataSource>>() {

            public Map<DbMediaSource, DataSource> apply(Long pipelineId) {
                // 构建第二层map
                return OtterMigrateMap.makeComputingMap(new Function<DbMediaSource, DataSource>() {

                    public DataSource apply(DbMediaSource dbMediaSource) {
                        return createDataSource(dbMediaSource.getUrl(), dbMediaSource.getUsername(),
                                                dbMediaSource.getPassword(), dbMediaSource.getDriver(),
                                                dbMediaSource.getType(), dbMediaSource.getEncode());
                    }

                });
            }
        });
    }

    public boolean support(DbMediaSource dbMediaSource) {
        return isMediaPushDataSource(dbMediaSource.getUrl());
    }

    public boolean support(DataSource dataSource) {
        if (dataSource == null) {
            return false;
        }
        return dataSource instanceof MediaPushDataSource;
    }

    public DataSource create(Long pipelineId, DbMediaSource dbMediaSource) {
        return dataSources.get(pipelineId).get(dbMediaSource);
    }

    protected DataSource createDataSource(String url, String userName, String password, String driverClassName,
                                          DataMediaType dataMediaType, String encoding) {
        MediaInfo media = parseMediaInfo(url);
        if (media == null) {
            if (isMediaPushDataSource(url)) {
                log.error("{} can't parse as an media groupdatasource, please check!", url);
            } else {
                log.info("{} is not a media datasource", url);
            }
            return null;
        }

        String groupKey = media.getGroupKey();
        MediaPushDataSource mediaDataSource = new MediaPushDataSource(url, userName, password, driverClassName,
                                                                      dataMediaType, encoding);
        mediaDataSource.setDbGroupKey(groupKey);
        mediaDataSource.init();
        return mediaDataSource;
    }

    @Override
    public boolean destory(Long pipelineId) {
        Map<DbMediaSource, DataSource> sources = dataSources.remove(pipelineId);
        if (sources != null) {
            for (DataSource dataSource : sources.values()) {
                try {
                    MediaPushDataSource mediaPushDataSource = (MediaPushDataSource) dataSource;
                    mediaPushDataSource.destory();
                } catch (SQLException e) {
                    log.error("ERROR ## close the datasource has an error", e);
                }
            }

            sources.clear();
        }

        return true;
    }

    public static boolean isMediaPushDataSource(String url) {
        return StringUtils.startsWithIgnoreCase(url, "jdbc:") && StringUtils.containsIgnoreCase(url, "groupKey");
    }

    // 解析 url
    public static MediaInfo parseMediaInfo(String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        Matcher matcher = PATTERN.matcher(url.trim());
        if (!matcher.matches()) {
            return null;
        }

        if (matcher.groupCount() < 1) {
            throw new IllegalArgumentException(url
                                               + " is a media push datasource but have no enough info for groupKey.");
        }
        return new MediaInfo(matcher.group(1));
    }

    public static class MediaInfo {

        String groupKey;

        public MediaInfo(String groupKey){
            this.groupKey = groupKey;
        }

        public String getGroupKey() {
            return groupKey;
        }

    }

}
