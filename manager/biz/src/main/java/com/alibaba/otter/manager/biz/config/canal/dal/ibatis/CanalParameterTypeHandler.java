package com.alibaba.otter.manager.biz.config.canal.dal.ibatis;

import java.sql.SQLException;

import com.alibaba.otter.canal.instance.manager.model.CanalParameter;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

/**
 * @author sarah.lij 2012-7-25 下午05:12:52
 */
public class CanalParameterTypeHandler implements TypeHandlerCallback {

    @Override
    public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
        setter.setString(JsonUtils.marshalToString(parameter));
    }

    @Override
    public Object getResult(ResultGetter getter) throws SQLException {
        return JsonUtils.unmarshalFromString(getter.getString(), CanalParameter.class);
    }

    public Object valueOf(String s) {
        return JsonUtils.unmarshalFromString(s, CanalParameter.class);
    }
}
