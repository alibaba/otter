package com.alibaba.otter.shared.arbitrate.model;

/**
 * S.E.T.L的数据传递对象
 * 
 * @author jianghang 2011-8-22 下午04:33:56
 */
public class EtlEventData extends ProcessEventData {

    private static final long serialVersionUID = -639227151519007664L;
    private Long              currNid;                                // 当前节点
    private Long              nextNid;                                // 下一个节点
    private Object            desc;                                   // 对应的pipe描述信息

    public Long getNextNid() {
        return nextNid;
    }

    public void setNextNid(Long nextNid) {
        this.nextNid = nextNid;
    }

    public Object getDesc() {
        return desc;
    }

    public void setDesc(Object desc) {
        this.desc = desc;
    }

    public Long getCurrNid() {
        return currNid;
    }

    public void setCurrNid(Long currNid) {
        this.currNid = currNid;
    }

}
