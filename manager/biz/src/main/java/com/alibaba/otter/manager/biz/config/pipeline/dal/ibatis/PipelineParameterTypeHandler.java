package com.alibaba.otter.manager.biz.config.pipeline.dal.ibatis;

import java.sql.SQLException;

import com.alibaba.otter.shared.common.model.config.pipeline.PipelineParameter;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

/**
 * 用于PipelineParameter的解析
 * 
 * @author simon
 */
public class PipelineParameterTypeHandler implements TypeHandlerCallback {

    @Override
    public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
        setter.setString(JsonUtils.marshalToStringWithoutTransient(parameter));
    }

    @Override
    public Object getResult(ResultGetter getter) throws SQLException {
        return JsonUtils.unmarshalFromString(getter.getString(), PipelineParameter.class);
    }

    public Object valueOf(String s) {
        return JsonUtils.unmarshalFromString(s, PipelineParameter.class);
    }

}
