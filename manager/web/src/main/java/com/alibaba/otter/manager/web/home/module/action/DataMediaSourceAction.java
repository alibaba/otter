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

package com.alibaba.otter.manager.web.home.module.action;

import javax.annotation.Resource;

import com.alibaba.citrus.service.form.CustomErrors;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.FormField;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.datamedia.DataMediaService;
import com.alibaba.otter.manager.biz.config.datamediasource.DataMediaSourceService;
import com.alibaba.otter.manager.web.common.WebConstant;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.common.model.config.data.mq.MqMediaSource;

public class DataMediaSourceAction extends AbstractAction {

    @Resource(name = "dataMediaSourceService")
    private DataMediaSourceService dataMediaSourceService;

    @Resource(name = "dataMediaService")
    private DataMediaService       dataMediaService;

    /**
     * 添加Channel
     * 
     * @param channelInfo
     * @param channelParameterInfo
     * @throws Exception
     */
    public void doAdd(@FormGroup("dataMediaSourceInfo") Group dataMediaSourceInfo,
                      @FormField(name = "formDataMediaSourceError", group = "dataMediaSourceInfo") CustomErrors err,
                      Navigator nav) throws Exception {
        DataMediaSource dataMediaSource = new DataMediaSource();
        dataMediaSourceInfo.setProperties(dataMediaSource);

        if (dataMediaSource.getType().isMysql() || dataMediaSource.getType().isOracle()) {
            DbMediaSource dbMediaSource = new DbMediaSource();
            dataMediaSourceInfo.setProperties(dbMediaSource);
            if (dataMediaSource.getType().isMysql()) {
                dbMediaSource.setDriver("com.mysql.jdbc.Driver");
            } else if (dataMediaSource.getType().isOracle()) {
                dbMediaSource.setDriver("oracle.jdbc.driver.OracleDriver");
            }
            try {
                dataMediaSourceService.create(dbMediaSource);
            } catch (RepeatConfigureException rce) {
                err.setMessage("invalidDataMediaSource");
                return;
            }
        } else if (dataMediaSource.getType().isNapoli() || dataMediaSource.getType().isMq()) {
            MqMediaSource mqMediaSource = new MqMediaSource();
            dataMediaSourceInfo.setProperties(mqMediaSource);

            try {
                dataMediaSourceService.create(mqMediaSource);
            } catch (RepeatConfigureException rce) {
                err.setMessage("invalidDataMediaSource");
                return;
            }
        }

        nav.redirectTo(WebConstant.DATA_MEDIA_SOURCE_LIST_LINK);
    }

    /**
     * @param channelId
     * @throws WebxException
     */
    public void doDelete(@Param("dataMediaSourceId") Long dataMediaSourceId, @Param("pageIndex") int pageIndex,
                         @Param("searchKey") String searchKey, Navigator nav) throws WebxException {
        if (dataMediaService.listByDataMediaSourceId(dataMediaSourceId).size() < 1) {
            dataMediaSourceService.remove(dataMediaSourceId);
        }

        nav.redirectToLocation("dataSourceList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey));
    }

    public void doEdit(@FormGroup("dataMediaSourceInfo") Group dataMediaSourceInfo, @Param("pageIndex") int pageIndex,
                       @Param("searchKey") String searchKey,
                       @FormField(name = "formDataMediaSourceError", group = "dataMediaSourceInfo") CustomErrors err,
                       Navigator nav) throws Exception {
        DbMediaSource dbMediaSource = new DbMediaSource();
        dataMediaSourceInfo.setProperties(dbMediaSource);

        if (dbMediaSource.getType().isMysql()) {
            dbMediaSource.setDriver("com.mysql.jdbc.Driver");
        } else if (dbMediaSource.getType().isOracle()) {
            dbMediaSource.setDriver("oracle.jdbc.driver.OracleDriver");
        }

        try {
            dataMediaSourceService.modify(dbMediaSource);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidDataMediaSource");
            return;
        }

        nav.redirectToLocation("dataSourceList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey));
    }

}
