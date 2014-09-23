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
import com.alibaba.otter.manager.biz.config.datamediapair.DataMediaPairService;
import com.alibaba.otter.manager.biz.config.datamediasource.DataMediaSourceService;
import com.alibaba.otter.manager.web.common.WebConstant;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.common.model.config.data.mq.MqMediaSource;

public class DataMediaAction extends AbstractAction {

    @Resource(name = "dataMediaService")
    private DataMediaService       dataMediaService;

    @Resource(name = "dataMediaPairService")
    private DataMediaPairService   dataMediaPairService;

    @Resource(name = "dataMediaSourceService")
    private DataMediaSourceService dataMediaSourceService;

    /**
     * 添加Channel
     * 
     * @param channelInfo
     * @param channelParameterInfo
     * @throws Exception
     */
    public void doAdd(@FormGroup("dataMediaInfo") Group dataMediaInfo,
                      @FormField(name = "formDataMediaError", group = "dataMediaInfo") CustomErrors err, Navigator nav)
                                                                                                                       throws Exception {

        DataMedia dataMedia = new DataMedia();
        dataMediaInfo.setProperties(dataMedia);
        DataMediaSource dataMediaSource = dataMediaSourceService.findById(dataMediaInfo.getField("sourceId").getLongValue());
        if (dataMediaSource.getType().isMysql() || dataMediaSource.getType().isOracle()) {
            dataMedia.setSource((DbMediaSource) dataMediaSource);
        } else if (dataMediaSource.getType().isNapoli() || dataMediaSource.getType().isMq()) {
            dataMedia.setSource((MqMediaSource) dataMediaSource);
        }

        try {
            dataMediaService.create(dataMedia);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidDataMedia");
            return;
        }

        nav.redirectTo(WebConstant.DATA_MEDIA_LIST_LINK);
    }

    /**
     * @param channelId
     * @throws WebxException
     */
    public void doDelete(@Param("dataMediaId") Long dataMediaId, @Param("pageIndex") int pageIndex,
                         @Param("searchKey") String searchKey, Navigator nav) throws WebxException {
        if (dataMediaPairService.listByDataMediaId(dataMediaId).size() < 1) {
            dataMediaService.remove(dataMediaId);
        }
        nav.redirectToLocation("dataMediaList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey));
    }

    public void doEdit(@FormGroup("dataMediaInfo") Group dataMediaInfo, @Param("pageIndex") int pageIndex,
                       @Param("searchKey") String searchKey,
                       @FormField(name = "formDataMediaError", group = "dataMediaInfo") CustomErrors err, Navigator nav)
                                                                                                                        throws Exception {
        DataMedia dataMedia = new DataMedia();
        dataMediaInfo.setProperties(dataMedia);
        DataMediaSource dataMediaSource = dataMediaSourceService.findById(dataMediaInfo.getField("sourceId").getLongValue());
        if (dataMediaSource.getType().isMysql() || dataMediaSource.getType().isOracle()) {
            dataMedia.setSource((DbMediaSource) dataMediaSource);
        } else if (dataMediaSource.getType().isNapoli() || dataMediaSource.getType().isMq()) {
            dataMedia.setSource((MqMediaSource) dataMediaSource);
        }

        try {
            dataMediaService.modify(dataMedia);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidDataMedia");
            return;
        }
        nav.redirectToLocation("dataMediaList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey));
    }
    /*
     * private boolean checkDataMedia(DbDataMedia dbDataMedia) { Connection conn
     * = null; Statement stmt = null; ResultSet rs = null; try { conn =
     * DriverManager.getConnection(dbDataMedia.getSource().getUrl(),
     * dbDataMedia.getSource().getUsername(),
     * dbDataMedia.getSource().getPassword()); if (null == conn) { return false;
     * } stmt = conn.createStatement(); rs = stmt.executeQuery("SELECT * FROM "
     * + dbDataMedia.getNamespace() + "." + dbDataMedia.getName() +
     * " where 0 = 1"); } catch (SQLException se) { return false; } finally {
     * try { if (null != rs) { rs.close(); } if (null != stmt) { stmt.close(); }
     * if (null != conn) { conn.close(); } } catch (SQLException e) {
     * e.printStackTrace(); } } return true; }
     */
}
