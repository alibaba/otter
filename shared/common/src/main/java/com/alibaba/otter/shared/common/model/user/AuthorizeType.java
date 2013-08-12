package com.alibaba.otter.shared.common.model.user;

/**
 * 用户权限
 * 
 * @author simon 2011-11-10 下午07:34:58
 */
public enum AuthorizeType {
    /** 匿名用户 */
    ANONYMOUS,
    /** 普通操作员 */
    OPERATOR,
    /** 系统管理员 */
    ADMIN;

    public boolean isAnonymous() {
        return this.equals(AuthorizeType.ANONYMOUS);
    }

    public boolean isOperator() {
        return this.equals(AuthorizeType.OPERATOR);
    }

    public boolean isAdmin() {
        return this.equals(AuthorizeType.ADMIN);
    }

}
