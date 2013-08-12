package com.alibaba.otter.node.etl.transform.transformer;

import com.alibaba.otter.shared.etl.model.FileData;

/**
 * {@linkplain FileData}数据对象转化
 * 
 * @author jianghang 2011-10-27 下午06:31:15
 * @version 4.0.0
 */
public class FileDataTransformer extends AbstractOtterTransformer<FileData, FileData> {

    public FileData transform(FileData data, OtterTransformerContext context) {
        // 后续可以针对文件进行目标地的fileResolver解析
        if (context.getDataMediaPair().getId().equals(data.getPairId())) {
            return data;
        } else {
            return null;
        }
        // data.setPairId(context.getDataMediaPair().getId());
        // return data;
    }

}
