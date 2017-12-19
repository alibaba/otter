package com.alibaba.otter.node.extend.processor;

import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class TradOrder_Processor extends AbstractEventProcessor {
    public boolean process(EventData eventData) {
        // 基本步骤：

        // 构造新的主键,老的主键uid插入普通值中
        Boolean ck_flag=false;
        for (EventColumn column : eventData.getKeys()) {
            if (column.getColumnName().toLowerCase().equals("outer_pay_status")){
                 if (column.getColumnValue().equals("1")) {
                     ck_flag = true;
                 }
            }
            if (column.getColumnName().toLowerCase().equals("pay_user_id")){
                if (column.isNull() || column.getColumnValue().length() <=0){
                    ck_flag=ck_flag&&true;
                } else
                {
                    ck_flag=false;
                }
            }
        }
        return ck_flag;
    }
}
