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

package com.alibaba.otter.manager.biz.config.parameter.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.config.parameter.SystemParameterService;
import com.alibaba.otter.manager.biz.config.parameter.dal.SystemParameterDAO;
import com.alibaba.otter.manager.biz.config.parameter.dal.dataobject.SystemParameterDO;
import com.alibaba.otter.shared.common.model.config.parameter.SystemParameter;

/**
 * @author sarah.lij 2012-4-13 下午04:32:48
 */
public class SystemParameterServiceImpl implements SystemParameterService {

    private static final Logger logger = LoggerFactory.getLogger(SystemParameterServiceImpl.class);

    private SystemParameterDAO  systemParameterDao;

    /**
     * 添加
     */
    public void createOrUpdate(SystemParameter systemParameter) {
        Assert.assertNotNull(systemParameter);
        try {
            SystemParameterDO systemParameterDo = modelToDo(systemParameter);
            systemParameterDo.setId(1L);
            systemParameterDao.insert(systemParameterDo); // 底层使用merge sql，不需要判断update
        } catch (Exception e) {
            logger.error("ERROR ## create SystemParameter has an exception!");
            throw new ManagerException(e);
        }
    }

    public SystemParameter find() {
        List<SystemParameterDO> systemParameterDos = systemParameterDao.listAll();
        if (systemParameterDos.isEmpty()) {
            logger.debug("DEBUG ## couldn't query any SystemParameter, maybe hasn't create any SystemParameter.");
            return new SystemParameter();
        } else {
            return doToModel(systemParameterDos.get(0));
        }
    }

    /**
     * 类型：数据库类型 Mysql和Oracle 用于Model对象转化为DO对象
     * 
     * @param SystemParameter
     * @return SystemParameterDO
     */
    private SystemParameterDO modelToDo(SystemParameter systemParameter) {
        SystemParameterDO systemParameterDo = new SystemParameterDO();
        systemParameterDo.setValue(systemParameter);
        return systemParameterDo;
    }

    /**
     * 类型：数据库类型 Mysql和Oracle 用于DO对象转化为Model对象
     * 
     * @param SystemParameterDo
     * @return SystemParameter
     */
    private SystemParameter doToModel(SystemParameterDO systemParameterDo) {
        return systemParameterDo.getValue();
    }

    /* ------------------------setter / getter--------------------------- */

    public void setsystemParameterDao(SystemParameterDAO systemParameterDao) {
        this.systemParameterDao = systemParameterDao;
    }

}
