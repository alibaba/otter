package com.alibaba.otter.canal.extend.ha;

import org.apache.commons.beanutils.BeanUtils;

import com.alibaba.otter.canal.common.CanalException;
import com.alibaba.otter.canal.parse.support.AuthenticationInfo;
import com.alibaba.otter.common.push.supplier.DatasourceInfo;

/**
 * @author zebin.xuzb 2013-1-23 下午4:54:33
 * @since 4.1.3
 */
public abstract class AuthenticationInfoUtils {

    private AuthenticationInfoUtils(){
    }

    public static AuthenticationInfo createFrom(DatasourceInfo datasourceInfo) {
        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        try {
            BeanUtils.copyProperties(datasourceInfo, authenticationInfo);
        } catch (Exception e) {
            throw new CanalException(e);
        }
        return authenticationInfo;
    }
}
