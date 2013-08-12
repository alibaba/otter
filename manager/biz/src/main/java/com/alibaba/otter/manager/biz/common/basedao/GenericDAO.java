package com.alibaba.otter.manager.biz.common.basedao;

import java.util.List;
import java.util.Map;

/**
 * @author simon 2011-10-31 上午09:40:47
 */
public interface GenericDAO<T> {

    public T insert(T entityObj);

    public void delete(Long identity);

    public void update(T entityObj);

    public List<T> listAll();

    public List<T> listByCondition(Map condition);

    public List<T> listByMultiId(Long... identities);

    public T findById(Long identity);

    public int getCount();

    public int getCount(Map condition);

    public boolean checkUnique(T entityObj);

}
