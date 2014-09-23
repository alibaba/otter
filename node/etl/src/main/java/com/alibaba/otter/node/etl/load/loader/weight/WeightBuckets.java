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

package com.alibaba.otter.node.etl.load.loader.weight;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * buckets的集合操作对象
 * 
 * @author jianghang 2011-11-1 上午07:37:05
 * @version 4.0.0
 */
public class WeightBuckets<T> {

    private List<WeightBucket<T>> buckets = new ArrayList<WeightBucket<T>>(); // 对应的桶信息

    /**
     * 获取对应的weight的列表，从小到大的排序结果
     */
    public synchronized List<Long> weights() {
        return Lists.transform(buckets, new Function<WeightBucket<T>, Long>() {

            public Long apply(WeightBucket<T> input) {
                return input.getWeight();
            }
        });
    }

    /**
     * 添加一个节点
     */
    public synchronized void addItem(long weight, T item) {
        WeightBucket<T> bucket = new WeightBucket<T>(weight);
        int index = indexedSearch(buckets, bucket);
        if (index > buckets.size() - 1) {// 先加一个bucket
            bucket.addLastItem(item);
            buckets.add(index, bucket);
        } else if (buckets.get(index).getWeight() != weight) {// 不匹配的
            bucket.addLastItem(item);
            buckets.add(index, bucket);
        } else {
            buckets.get(index).addLastItem(item);// 添加到已有的bucket上
        }

    }

    public synchronized List<T> getItems(long weight) {
        WeightBucket<T> bucket = new WeightBucket<T>(weight);
        int index = indexedSearch(buckets, bucket);
        if (index < buckets.size() && index >= 0) {
            return buckets.get(index).getBucket();
        } else {
            return new LinkedList<T>();
        }
    }

    // ========================= helper method =====================

    private int indexedSearch(List<WeightBucket<T>> list, WeightBucket<T> item) {
        int i = 0;
        for (; i < list.size(); i++) {
            Comparable midVal = list.get(i);
            int cmp = midVal.compareTo(item);
            if (cmp == 0) {// item等于中间值
                return i;
            } else if (cmp > 0) {// item比中间值小
                return i;
            } else if (cmp < 0) {// item比中间值大
                // next
            }
        }

        return i;
    }

}

/**
 * 相同weight的item集合对象
 * 
 * @author jianghang 2011-11-1 上午11:09:58
 * @version 4.0.0
 * @param <T>
 */
class WeightBucket<T> implements Comparable<WeightBucket> {

    private long          weight = -1;
    private LinkedList<T> bucket = new LinkedList<T>();

    public WeightBucket(){
    }

    public WeightBucket(long weight){
        this.weight = weight;
    }

    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    public List<T> getBucket() {
        return bucket;
    }

    public void setBucket(LinkedList<T> bucket) {
        this.bucket = bucket;
    }

    public void addFirstItem(T item) {
        this.bucket.addFirst(item);
    }

    public T getFirstItem() {
        return this.bucket.getFirst();
    }

    public void addLastItem(T item) {
        this.bucket.addLast(item);
    }

    public T getLastItem() {
        return this.bucket.getLast();
    }

    public int compareTo(WeightBucket o) {
        if (this.getWeight() > o.getWeight()) {
            return 1;
        } else if (this.getWeight() == o.getWeight()) {
            return 0;
        } else {
            return -1;
        }
    }

}
