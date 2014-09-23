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
