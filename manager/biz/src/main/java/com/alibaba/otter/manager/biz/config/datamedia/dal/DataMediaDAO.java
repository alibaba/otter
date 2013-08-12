package com.alibaba.otter.manager.biz.config.datamedia.dal;

import java.util.List;

import com.alibaba.otter.manager.biz.common.basedao.GenericDAO;
import com.alibaba.otter.manager.biz.config.datamedia.dal.dataobject.DataMediaDO;

/**
 * @author simon
 */
public interface DataMediaDAO extends GenericDAO<DataMediaDO> {

    public List<DataMediaDO> listByDataMediaSourceId(Long dataMediaSourceId);

    public DataMediaDO checkUniqueAndReturnExist(DataMediaDO dataMedia);
}
