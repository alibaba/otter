package com.alibaba.otter.shared.common.utils.code;

/**
 * 返回值定义的code对象定义
 * 
 * @author jianghang 2011-9-13 下午04:46:42
 */
public interface Code {

    public String getCode();

    public String getMessage(String... params);
}
