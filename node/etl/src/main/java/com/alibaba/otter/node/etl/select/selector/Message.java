package com.alibaba.otter.node.etl.select.selector;

import java.util.List;

/**
 * 数据对象
 * 
 * @author jianghang 2012-7-31 下午02:43:08
 */
public class Message<T> {

    private Long    id;
    private List<T> datas;

    public Message(Long id, List<T> datas){
        this.id = id;
        this.datas = datas;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<T> getDatas() {
        return datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
    }

}
