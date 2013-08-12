package com.alibaba.otter.manager.biz.config.parameter.dal.ibatis;

import java.sql.SQLException;

import com.alibaba.otter.shared.common.model.config.parameter.SystemParameter;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

/**
 * 用于SystemParameter的解析
 * 
 * @author simon
 */
public class SystemParameterTypeHandler implements TypeHandlerCallback {

    @Override
    public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
        setter.setString(JsonUtils.marshalToString(parameter));
    }

    @Override
    public Object getResult(ResultGetter getter) throws SQLException {
        return JsonUtils.unmarshalFromString(getter.getString(), SystemParameter.class);
    }

    public Object valueOf(String s) {
        return JsonUtils.unmarshalFromString(s, SystemParameter.class);
    }

}
