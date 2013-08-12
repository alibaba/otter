package com.alibaba.otter.node.etl.load;

import java.util.List;

import org.testng.annotations.Test;

import com.alibaba.otter.node.etl.load.loader.weight.WeightBuckets;
import com.alibaba.otter.node.etl.BaseOtterTest;

public class WeightBucketTest extends BaseOtterTest {

    @Test
    public void test_simple() {
        WeightBuckets<String> bucket = new WeightBuckets();
        int iCount = 10;
        int jCount = 100;
        for (int i = 0; i < iCount; i++) {
            for (int j = 0; j < jCount; j++) {
                bucket.addItem(i, "name:" + i + "_" + j);
            }
        }

        for (int i = 0; i < iCount; i++) {
            List<String> items = bucket.getItems(i);
            want.collection(items).sizeEq(jCount);
            for (int j = 0; j < jCount; j++) {
                want.string(items.get(j)).isEqualTo("name:" + i + "_" + j);
            }
        }
    }

    @Test
    public void test_random() {
        WeightBuckets<String> bucket = new WeightBuckets();
        int iCount = 10;
        int jCount = 100;
        for (int i = 0; i < iCount; i++) {
            for (int j = 0; j < jCount; j++) {
                bucket.addItem(j, "name:" + i + "_" + j);
            }
        }

        for (int j = 0; j < jCount; j++) {
            List<String> items = bucket.getItems(j);
            want.collection(items).sizeEq(iCount);

            for (int i = 0; i < iCount; i++) {
                want.string(items.get(i)).isEqualTo("name:" + i + "_" + j);
            }
        }
    }

    @Test
    public void test_custom() {
        WeightBuckets<String> bucket = new WeightBuckets();
        bucket.addItem(6, "6");
        bucket.addItem(1, "1");
        bucket.addItem(5, "5");
        bucket.addItem(3, "3");
        bucket.addItem(5, "51");
        bucket.addItem(2, "2");
        bucket.addItem(4, "4");
        bucket.addItem(6, "61");

        List<Long> weights = bucket.weights();
        want.number(weights.get(0)).isEqualTo(1);
        want.number(weights.get(1)).isEqualTo(2);
        want.number(weights.get(2)).isEqualTo(3);
        want.number(weights.get(3)).isEqualTo(4);
        want.number(weights.get(4)).isEqualTo(5);
        want.number(weights.get(5)).isEqualTo(6);
    }
}
