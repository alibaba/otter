package com.alibaba.otter.manager.biz.config.datamedia;

import java.util.List;

import com.alibaba.otter.manager.biz.common.baseservice.GenericService;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;

/**
 * @author simon
 */
public interface DataMediaService extends GenericService<DataMedia> {

    // public List<DataMedia> listDataMediaByIds(Long... dataMediaIds);

    public List<DataMedia> listByDataMediaSourceId(Long dataMediaSourceId);

    public Long createReturnId(DataMedia dataMedia);

    public List<String> queryColumnByMedia(DataMedia dataMedia);

    public List<String> queryColumnByMediaId(Long dataMediaId);

}
