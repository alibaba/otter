package com.alibaba.otter.manager.biz.config.canal;

import java.util.List;
import java.util.Map;

import com.alibaba.otter.canal.instance.manager.model.Canal;

/**
 * @author sarah.lij 2012-7-25 下午04:02:20
 */
public interface CanalService {

    public void create(Canal canal);

    public void remove(Long canalId);

    public void modify(Canal canal);

    public List<Canal> listByIds(Long... identities);

    public List<Canal> listAll();

    public Canal findById(Long canalId);

    public Canal findByName(String name);

    public int getCount(Map condition);

    public List<Canal> listByCondition(Map condition);

}
