package com.alibaba.otter.manager.biz.common.baseservice;

import java.util.List;
import java.util.Map;

/**
 * @author simon 2011-10-31 上午10:34:17
 */
public interface GenericService<T> {

    public void create(T entityObj);

    public void remove(Long identity);

    public void modify(T entityObj);

    public T findById(Long identity);

    public List<T> listByIds(Long... identities);

    public List<T> listAll();

    public List<T> listByCondition(Map condition);

    public int getCount();

    public int getCount(Map condition);
}
