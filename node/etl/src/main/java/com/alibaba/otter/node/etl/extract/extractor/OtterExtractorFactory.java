package com.alibaba.otter.node.etl.extract.extractor;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.Assert;

import com.alibaba.otter.shared.etl.model.DbBatch;

/**
 * otter extractor工厂处理
 * 
 * @author jianghang 2012-4-18 下午04:09:15
 * @version 4.0.2
 */
public class OtterExtractorFactory implements BeanFactoryAware {

    private List        dbBatchExtractor;
    private BeanFactory beanFactory;

    public void extract(DbBatch dbBatch) {
        Assert.notNull(dbBatch);
        for (Object extractor : dbBatchExtractor) {
            OtterExtractor otterExtractor = null;
            if (extractor instanceof java.lang.String) {
                // 每次从容器中取一次，有做池化处理
                otterExtractor = (OtterExtractor) beanFactory.getBean((String) extractor, OtterExtractor.class);
            } else {
                otterExtractor = (OtterExtractor) extractor;
            }

            otterExtractor.extract(dbBatch);
        }
    }

    // ================== setter / getter ====================

    public void setDbBatchExtractor(List dbBatchExtractor) {
        this.dbBatchExtractor = dbBatchExtractor;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
