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

package com.alibaba.otter.manager.biz.statistics.stage.dal.ibatis;

import java.util.List;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.alibaba.otter.manager.biz.statistics.stage.dal.ProcessDAO;
import com.alibaba.otter.manager.biz.statistics.stage.dal.dataobject.ProcessStatDO;

/**
 * TODO Comment of IbatisProcessStatDAO
 * 
 * @author danping.yudp
 */
public class IbatisProcessDAO extends SqlMapClientDaoSupport implements ProcessDAO {

    @Override
    public void insertProcessStat(ProcessStatDO processStat) {

        getSqlMapClientTemplate().insert("", processStat);
    }

    @Override
    public void deleteProcessStat(Long processId) {
        getSqlMapClientTemplate().delete("", processId);
    }

    @Override
    public void modifyProcessStat(ProcessStatDO processStat) {
        getSqlMapClientTemplate().update("", processStat);
    }

    @Override
    public ProcessStatDO findByProcessId(Long processId) {

        return (ProcessStatDO) getSqlMapClientTemplate().queryForObject("", processId);
    }

    @Override
    public List<ProcessStatDO> listAllProcessStat() {

        return (List<ProcessStatDO>) getSqlMapClientTemplate().queryForList("");
    }

    @Override
    public List<ProcessStatDO> listProcessStatsByPipelineId(Long pipelineId) {

        return (List<ProcessStatDO>) getSqlMapClientTemplate().queryForList("", pipelineId);
    }

}
