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

package com.alibaba.otter.node.etl.common.db.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.otter.node.etl.common.datasource.DataSourceService;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.google.common.base.Function;
import com.google.common.collect.GenericMapMaker;
import com.google.common.collect.MapEvictionListener;
import com.google.common.collect.MapMaker;

/**
 * @author jianghang 2011-10-27 下午02:12:06
 * @version 4.0.0
 */
public class DbDialectFactory implements DisposableBean {

    private static final Logger                      logger = LoggerFactory.getLogger(DbDialectFactory.class);
    private DataSourceService                        dataSourceService;
    private DbDialectGenerator                       dbDialectGenerator;

    // 第一层pipelineId , 第二层DbMediaSource id
    private Map<Long, Map<DbMediaSource, DbDialect>> dialects;

    public DbDialectFactory(){
        // 构建第一层map
        GenericMapMaker mapMaker = null;
        mapMaker = new MapMaker().softValues()
            .evictionListener(new MapEvictionListener<Long, Map<DbMediaSource, DbDialect>>() {

                public void onEviction(Long pipelineId, Map<DbMediaSource, DbDialect> dialect) {
                    if (dialect == null) {
                        return;
                    }

                    for (DbDialect dbDialect : dialect.values()) {
                        dbDialect.destory();
                    }
                }
            });

        dialects = mapMaker.makeComputingMap(new Function<Long, Map<DbMediaSource, DbDialect>>() {

            public Map<DbMediaSource, DbDialect> apply(final Long pipelineId) {
                // 构建第二层map
                return new MapMaker().makeComputingMap(new Function<DbMediaSource, DbDialect>() {

                    public DbDialect apply(final DbMediaSource source) {
                        DataSource dataSource = dataSourceService.getDataSource(pipelineId, source);
                        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                        return (DbDialect) jdbcTemplate.execute(new ConnectionCallback() {

                            public Object doInConnection(Connection c) throws SQLException, DataAccessException {
                                DatabaseMetaData meta = c.getMetaData();
                                String databaseName = meta.getDatabaseProductName();
                                String databaseVersion = meta.getDatabaseProductVersion();
                                int databaseMajorVersion = meta.getDatabaseMajorVersion();
                                int databaseMinorVersion = meta.getDatabaseMinorVersion();
                                DbDialect dialect = dbDialectGenerator.generate(jdbcTemplate,
                                    databaseName,
                                    databaseVersion,
                                    databaseMajorVersion,
                                    databaseMinorVersion,
                                    source.getType());
                                if (dialect == null) {
                                    throw new UnsupportedOperationException("no dialect for" + databaseName);
                                }

                                if (logger.isInfoEnabled()) {
                                    logger.info(String.format("--- DATABASE: %s, SCHEMA: %s ---",
                                        databaseName,
                                        (dialect.getDefaultSchema() == null) ? dialect.getDefaultCatalog() : dialect.getDefaultSchema()));
                                }

                                return dialect;
                            }
                        });

                    }
                });
            }
        });

    }

    public DbDialect getDbDialect(Long pipelineId, DbMediaSource source) {
        return dialects.get(pipelineId).get(source);
    }

    public void destory(Long pipelineId) {
        Map<DbMediaSource, DbDialect> dialect = dialects.remove(pipelineId);
        if (dialect != null) {
            for (DbDialect dbDialect : dialect.values()) {
                dbDialect.destory();
            }
        }
    }

    public void destroy() throws Exception {
        Set<Long> pipelineIds = new HashSet<Long>(dialects.keySet());
        for (Long pipelineId : pipelineIds) {
            destory(pipelineId);
        }
    }

    // =============== setter / getter =================

    public void setDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    public void setDbDialectGenerator(DbDialectGenerator dbDialectGenerator) {
        this.dbDialectGenerator = dbDialectGenerator;
    }

}
