package com.alibaba.otter.shared.arbitrate.impl.setl.helper;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.common.model.config.enums.StageType;
import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * stage进度数据
 * 
 * @author jianghang 2012-9-29 上午10:34:05
 * @version 4.1.0
 */
public class StageProgress {

    private StageType    stage;
    private EtlEventData data;

    public StageProgress(){
    }

    public StageProgress(StageType stage, EtlEventData data){
        this.stage = stage;
        this.data = data;
    }

    public StageType getStage() {
        return stage;
    }

    public void setStage(StageType stage) {
        this.stage = stage;
    }

    public EtlEventData getData() {
        return data;
    }

    public void setData(EtlEventData data) {
        this.data = data;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }
}
