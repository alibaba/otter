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

package com.alibaba.otter.common.push;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * @author zebin.xuzb 2013-1-23 下午2:07:28
 * @since 4.1.3
 */
public class PushException extends NestableRuntimeException {

    private static final long serialVersionUID = -1223749329887228066L;

    public PushException(){
        super();
    }

    public PushException(String msg, Throwable cause){
        super(msg, cause);
    }

    public PushException(String msg){
        super(msg);
    }

    public PushException(Throwable cause){
        super(cause);
    }

}
