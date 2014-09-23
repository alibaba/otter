/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.node.etl.transform.transformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.transform.exception.TransformException;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.db.DbDataMedia;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.etl.model.BatchObject;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.FileBatch;
import com.alibaba.otter.shared.etl.model.FileData;
import com.alibaba.otter.shared.etl.model.Identity;
import com.alibaba.otter.shared.etl.model.RowBatch;

/**
 * 数据对象转化工厂
 * 
 * @author jianghang 2011-10-27 下午06:29:02
 * @version 4.0.0
 */
public class OtterTransformerFactory {

    private ConfigClientService configClientService;
    private RowDataTransformer  rowDataTransformer;
    private FileDataTransformer fileDataTransformer;

    /**
     * 将一种源数据进行转化，最后得到的结果会根据DataMediaPair中定义的目标对象生成不同的数据对象 <br/>
     * 
     * <pre>
     * 返回对象格式：Map
     * key : Class对象，代表生成的目标数据对象
     * value : 每种目标数据对象的集合数据
     * </pre>
     */
    public Map<Class, BatchObject> transform(RowBatch rowBatch) {
        final Identity identity = translateIdentity(rowBatch.getIdentity());
        Map<Class, BatchObject> result = new HashMap<Class, BatchObject>();
        // 初始化默认值
        result.put(EventData.class, initBatchObject(identity, EventData.class));

        for (EventData eventData : rowBatch.getDatas()) {
            // 处理eventData
            Long tableId = eventData.getTableId();
            Pipeline pipeline = configClientService.findPipeline(identity.getPipelineId());
            // 针对每个同步数据，可能会存在多路复制的情况
            List<DataMediaPair> dataMediaPairs = ConfigHelper.findDataMediaPairByMediaId(pipeline, tableId);
            for (DataMediaPair pair : dataMediaPairs) {
                if (!pair.getSource().getId().equals(tableId)) { // 过滤tableID不为源的同步
                    continue;
                }

                OtterTransformer translate = lookup(pair.getSource(), pair.getTarget());
                // 进行转化
                Object item = translate.transform(eventData, new OtterTransformerContext(identity, pair, pipeline));
                if (item == null) {
                    continue;
                }
                // 合并结果
                merge(identity, result, item);
            }

        }

        return result;
    }

    /**
     * 转化FileBatch对象
     */
    public Map<Class, BatchObject> transform(FileBatch fileBatch) {
        final Identity identity = translateIdentity(fileBatch.getIdentity());
        List<FileData> fileDatas = fileBatch.getFiles();
        Map<Class, BatchObject> result = new HashMap<Class, BatchObject>();
        // 初始化默认值
        result.put(FileData.class, initBatchObject(identity, FileData.class));

        for (FileData fileData : fileDatas) {
            // 进行转化
            Long tableId = fileData.getTableId();
            Pipeline pipeline = configClientService.findPipeline(identity.getPipelineId());
            // 针对每个同步数据，可能会存在多路复制的情况
            List<DataMediaPair> dataMediaPairs = ConfigHelper.findDataMediaPairByMediaId(pipeline, tableId);
            for (DataMediaPair pair : dataMediaPairs) {
                if (!pair.getSource().getId().equals(tableId)) { // 过滤tableID不为源的同步
                    continue;
                }

                Object item = fileDataTransformer.transform(fileData, new OtterTransformerContext(identity, pair,
                                                                                                  pipeline));
                if (item == null) {
                    continue;
                }
                // 合并结果
                merge(identity, result, item);
            }

        }

        return result;
    }

    // =============================== helper method
    // ============================

    // 将生成的item对象合并到结果对象中
    private synchronized void merge(Identity identity, Map<Class, BatchObject> data, Object item) {
        Class clazz = item.getClass();
        BatchObject batchObject = data.get(clazz);
        // 初始化一下对象
        if (batchObject == null) {
            batchObject = initBatchObject(identity, clazz);
            data.put(clazz, batchObject);
        }

        // 进行merge处理
        if (batchObject instanceof RowBatch) {
            ((RowBatch) batchObject).merge((EventData) item);
        } else if (batchObject instanceof FileBatch) {
            ((FileBatch) batchObject).getFiles().add((FileData) item);
        } else {
            throw new TransformException("no support Data[" + clazz.getName() + "]");
        }
    }

    // 根据对应的类型初始化batchObject对象
    private BatchObject initBatchObject(Identity identity, Class clazz) {
        if (EventData.class.equals(clazz)) {
            RowBatch rowbatch = new RowBatch();
            rowbatch.setIdentity(identity);
            return rowbatch;
        } else if (FileData.class.equals(clazz)) {
            FileBatch fileBatch = new FileBatch();
            fileBatch.setIdentity(identity);
            return fileBatch;
        } else {
            throw new TransformException("no support Data[" + clazz.getName() + "]");
        }
    }

    // 查找对应的tranlate转化对象
    private OtterTransformer lookup(DataMedia sourceDataMedia, DataMedia targetDataMedia) {
        if (sourceDataMedia instanceof DbDataMedia && targetDataMedia instanceof DbDataMedia) {
            return rowDataTransformer;
        }

        throw new TransformException("no support translate for source " + sourceDataMedia.toString() + " to target "
                                     + targetDataMedia);
    }

    private Identity translateIdentity(Identity identity) {
        Identity result = new Identity();
        result.setChannelId(identity.getChannelId());
        result.setPipelineId(identity.getPipelineId());
        result.setProcessId(identity.getProcessId());
        return result;
    }

    // ==================== setter / getter ==================

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

    public void setRowDataTransformer(RowDataTransformer rowDataTransformer) {
        this.rowDataTransformer = rowDataTransformer;
    }

    public void setFileDataTransformer(FileDataTransformer fileDataTransformer) {
        this.fileDataTransformer = fileDataTransformer;
    }

}
