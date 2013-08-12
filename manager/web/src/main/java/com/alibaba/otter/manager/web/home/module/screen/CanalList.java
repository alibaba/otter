package com.alibaba.otter.manager.web.home.module.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.util.Paginator;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.manager.biz.config.canal.CanalService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.web.common.model.SeniorCanal;

/**
 * @author sarah.lij 2012-7-26 下午04:25:52
 */
public class CanalList {

    @Resource(name = "canalService")
    private CanalService    canalService;

    @Resource(name = "pipelineService")
    private PipelineService pipelineService;

    public void execute(@Param("pageIndex") int pageIndex, @Param("searchKey") String searchKey, Context context)
                                                                                                                 throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> condition = new HashMap<String, Object>();
        if ("请输入关键字(目前支持Node的ID、名字搜索)".equals(searchKey)) {
            searchKey = "";
        }
        condition.put("searchKey", searchKey);

        int count = canalService.getCount(condition);
        Paginator paginator = new Paginator();
        paginator.setItems(count);
        paginator.setPage(pageIndex);

        condition.put("offset", paginator.getOffset());
        condition.put("length", paginator.getLength());

        List<Canal> canals = canalService.listByCondition(condition);

        List<SeniorCanal> seniorCanals = new ArrayList<SeniorCanal>();

        for (Canal canal : canals) {
            SeniorCanal seniorCanal = new SeniorCanal();
            seniorCanal.setId(canal.getId());
            seniorCanal.setName(canal.getName());
            seniorCanal.setStatus(canal.getStatus());
            seniorCanal.setDesc(canal.getDesc());
            seniorCanal.setCanalParameter(canal.getCanalParameter());
            seniorCanal.setUsed(false);
            seniorCanal.setGmtCreate(canal.getGmtCreate());
            seniorCanal.setGmtModified(canal.getGmtModified());
            seniorCanals.add(seniorCanal);
        }
        context.put("seniorCanals", seniorCanals);
        context.put("paginator", paginator);
        context.put("searchKey", searchKey);
    }
}
