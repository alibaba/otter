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

package com.alibaba.otter.shared.arbitrate.model;

/**
 * 结束信号对象
 * 
 * @author jianghang 2011-9-26 上午11:34:12
 * @version 4.0.0
 */
public class TerminEventData extends ProcessEventData {

    private static final long serialVersionUID = -5108807540865997596L;

    public static enum TerminType {
        /** 正常结束 */
        NORMAL,
        /** 警告信息 */
        WARNING,
        /** 回滚对应同步 */
        ROLLBACK,
        /** 重新开始同步 */
        RESTART,
        /** 关闭同步 */
        SHUTDOWN;

        public boolean isNormal() {
            return this.equals(NORMAL);
        }

        public boolean isWarning() {
            return this.equals(WARNING);
        }

        public boolean isRollback() {
            return this.equals(ROLLBACK);
        }

        public boolean isRestart() {
            return this.equals(RESTART);
        }

        public boolean isShutdown() {
            return this.equals(SHUTDOWN);
        }
    }

    private TerminType type = TerminType.NORMAL;
    private String     code;
    private String     desc;
    private Long       currNid;

    public TerminType getType() {
        return type;
    }

    public void setType(TerminType type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Long getCurrNid() {
        return currNid;
    }

    public void setCurrNid(Long currNid) {
        this.currNid = currNid;
    }

}
