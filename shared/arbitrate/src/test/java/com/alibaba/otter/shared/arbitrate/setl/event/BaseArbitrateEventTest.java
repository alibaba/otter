package com.alibaba.otter.shared.arbitrate.setl.event;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.alibaba.otter.shared.arbitrate.impl.setl.MainStemArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.setl.BaseStageTest;

/**
 * 提供mainStem信号的初始化
 * 
 * @author jianghang 2011-9-22 下午05:30:08
 * @version 4.0.0
 */
public class BaseArbitrateEventTest extends BaseStageTest {

    protected MainStemArbitrateEvent mainStemEvent;

    @BeforeMethod
    public void setUp() {
        nodeEvent.init(nid);
        channelEvent.start(channelId);//启动
    }

    @AfterMethod
    public void tearDown() {
        nodeEvent.destory(local.getId());
    }
}
