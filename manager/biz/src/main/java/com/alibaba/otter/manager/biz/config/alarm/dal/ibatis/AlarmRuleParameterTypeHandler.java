package com.alibaba.otter.manager.biz.config.alarm.dal.ibatis;

import java.sql.SQLException;

import com.alibaba.otter.manager.biz.config.alarm.dal.dataobject.AlarmRuleParameter;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

/**
 * 用于AlarmRuleParameter的解析
 * 
 * @author simon
 */
public class AlarmRuleParameterTypeHandler implements TypeHandlerCallback {

    @Override
    public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
        setter.setString(JsonUtils.marshalToString(parameter));
    }

    @Override
    public Object getResult(ResultGetter getter) throws SQLException {
        return JsonUtils.unmarshalFromString(getter.getString(), AlarmRuleParameter.class);
    }

    public Object valueOf(String s) {
        return JsonUtils.unmarshalFromString(s, AlarmRuleParameter.class);
    }

}
