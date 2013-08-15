package com.alibaba.otter.manager.web.home.module.screen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.util.Paginator;
import com.alibaba.otter.manager.biz.config.datamedia.DataMediaService;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;

public class SelectDataMedia {

    @Resource(name = "dataMediaService")
    private DataMediaService dataMediaService;

    public void execute(@Param("pageIndex") int pageIndex, @Param("searchKey") String searchKey,
                        @Param("local") String local, Context context) throws Exception {
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
        context.put("dataMedias", dataMedias);
        context.put("paginator", paginator);
        context.put("searchKey", searchKey);
        context.put("local", local);
    }
}
