package com.alibaba.otter.manager.web.home.module.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.util.Paginator;
import com.alibaba.otter.manager.biz.config.datamedia.DataMediaService;
import com.alibaba.otter.manager.biz.config.datamediapair.DataMediaPairService;
import com.alibaba.otter.manager.web.common.model.SeniorDataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;

public class DataMediaList {

    @Resource(name = "dataMediaService")
    private DataMediaService     dataMediaService;

    @Resource(name = "dataMediaPairService")
    private DataMediaPairService dataMediaPairService;

    public void execute(@Param("pageIndex") int pageIndex, @Param("searchKey") String searchKey, Context context)
                                                                                                                 throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> condition = new HashMap<String, Object>();
        if ("请输入关键字(目前支持DataMedia的ID、名字搜索)".equals(searchKey)) {
            searchKey = "";
        }
        condition.put("searchKey", searchKey);

        int count = dataMediaService.getCount(condition);
        Paginator paginator = new Paginator();
        paginator.setItems(count);
        paginator.setPage(pageIndex);

        condition.put("offset", paginator.getOffset());
        condition.put("length", paginator.getLength());

        List<DataMedia> dataMedias = dataMediaService.listByCondition(condition);
        List<SeniorDataMedia> seniorDataMedias = new ArrayList<SeniorDataMedia>();
        for (DataMedia dataMedia : dataMedias) {
            SeniorDataMedia seniorDataMedia = new SeniorDataMedia();
            seniorDataMedia.setId(dataMedia.getId());
            seniorDataMedia.setEncode(dataMedia.getEncode());
            seniorDataMedia.setGmtCreate(dataMedia.getGmtCreate());
            seniorDataMedia.setGmtModified(dataMedia.getGmtModified());
            seniorDataMedia.setName(dataMedia.getName());
            seniorDataMedia.setNamespace(dataMedia.getNamespace());
            seniorDataMedia.setSource(dataMedia.getSource());
            List<DataMediaPair> pairs = dataMediaPairService.listByDataMediaId(dataMedia.getId());
            seniorDataMedia.setPairs(pairs);
            if (pairs.size() < 1) {
                seniorDataMedia.setUsed(false);
            } else {
                seniorDataMedia.setUsed(true);
            }
            seniorDataMedias.add(seniorDataMedia);
        }

        context.put("dataMedias", seniorDataMedias);
        context.put("paginator", paginator);
        context.put("searchKey", searchKey);
    }
}
