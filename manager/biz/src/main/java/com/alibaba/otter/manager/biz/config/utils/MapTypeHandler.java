package com.alibaba.otter.manager.biz.config.utils;

import java.sql.SQLException;
import java.util.Map;

import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

/**
 * 用于Map数据结构的解析，ibatis相关
 * 
 * @author simon
 */
public class MapTypeHandler implements TypeHandlerCallback {

    @Override
    public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
        setter.setString(JsonUtils.marshalToString(parameter));
    }

    @Override
    public Object getResult(ResultGetter getter) throws SQLException {
        return JsonUtils.unmarshalFromString(getter.getString(), Map.class);
    }

    public Object valueOf(String s) {
        return JsonUtils.unmarshalFromString(s, Map.class);
    }

}
