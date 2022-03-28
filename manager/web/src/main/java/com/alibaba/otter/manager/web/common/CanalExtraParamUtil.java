package com.alibaba.otter.manager.web.common;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.citrus.service.form.Group;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter;

public class CanalExtraParamUtil {

    public static Map<String, Object> getExtraParamMap(CanalParameter canalParameter) {
        CanalParameter.SourcingType sourcingType = canalParameter.getSourcingType();
        if (sourcingType != null && "OCEANBASE".equalsIgnoreCase(sourcingType.name())) {
            return JSONObject.parseObject(canalParameter.getLocalBinlogDirectory());
        }
        return null;
    }

    public static void setExtraParamString(CanalParameter canalParameter, Group canalParameterInfo) {
        CanalParameter.SourcingType sourcingType = canalParameter.getSourcingType();
        String localBinlogDirectory = canalParameterInfo.getField("localBinlogDirectory").getStringValue();
        if (StringUtils.isNotEmpty(localBinlogDirectory)) {
            canalParameter.setLocalBinlogDirectory(localBinlogDirectory);
        } else if (sourcingType != null && "OCEANBASE".equalsIgnoreCase(sourcingType.name())) {
            JSONObject extraParam = new JSONObject();
            extraParam.put("rsList", canalParameterInfo.getField("rsList").getStringValue());
            extraParam.put("tenant", canalParameterInfo.getField("tenant").getStringValue());
            canalParameter.setLocalBinlogDirectory(extraParam.toJSONString());
        }
    }
}
