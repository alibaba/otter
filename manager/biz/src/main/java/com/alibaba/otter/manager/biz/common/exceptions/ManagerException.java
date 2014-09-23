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

package com.alibaba.otter.manager.biz.common.exceptions;

/**
 * ManagerException for Manager Model
 * 
 * @author simon 2011-11-13 下午07:38:47
 */
public class ManagerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ManagerException(String cause){
        super(cause);
    }

    public ManagerException(Throwable t){
        super(t);
    }

    public ManagerException(String cause, Throwable t){
        super(cause, t);
    }

}
