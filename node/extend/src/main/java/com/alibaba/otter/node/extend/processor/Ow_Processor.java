package com.alibaba.otter.node.extend.processor;

import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class Ow_Processor  extends AbstractEventProcessor {
    public boolean process(EventData eventData) {
        // 基本步骤：

        // 构造新的主键,老的主键uid插入普通值中
        EventColumn id = new EventColumn();
        id.setColumnValue(eventData.getSchemaName());
        id.setColumnType(Types.BIGINT);
        id.setColumnName("uid");

        EventColumn room = new EventColumn();
        room.setColumnValue(eventData.getSchemaName());
        room.setColumnType(Types.BIGINT);
        room.setColumnName("toproomid");

        for (EventColumn column : eventData.getKeys()) {
            if (column.getColumnName().toLowerCase().equals("uid")){
                id.setColumnValue(column.getColumnValue());
            }
            if (column.getColumnName().toLowerCase().equals("toproomid")){
                room.setColumnValue(column.getColumnValue());
            }
        }
        List<EventColumn> keys = new ArrayList<EventColumn>();
        keys.add(room);
        eventData.setKeys(keys);

        eventData.getColumns().add(id);
        boolean v_flag =false;
        for (EventColumn column : eventData.getColumns()) {
            if (column.getColumnName().toLowerCase().equals("rank")){
                if (column.getColumnValue().equals("80")){
                    v_flag= true;
                }
                else{
                    return false;
                }
            }
            if (column.getColumnName().toLowerCase().equals("status")){
                if (column.getColumnValue().equals("0") && v_flag){
                    return true;
                }
                else{
                    return false;
                }
            }
        }
        return true;
    }
}
