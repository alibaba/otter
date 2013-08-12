package com.alibaba.otter.manager.biz.config.channel.dal.ibatis;

import java.sql.SQLException;

import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

/**
 * 用于ChannelParameter的解析
 * 
 * @author simon
 */
public class ChannelParameterTypeHandler implements TypeHandlerCallback {

    @Override
    public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
        setter.setString(JsonUtils.marshalToString(parameter));
    }

    @Override
    public Object getResult(ResultGetter getter) throws SQLException {
        return JsonUtils.unmarshalFromString(getter.getString(), ChannelParameter.class);
    }

    public Object valueOf(String s) {
        return JsonUtils.unmarshalFromString(s, ChannelParameter.class);
    }

}
