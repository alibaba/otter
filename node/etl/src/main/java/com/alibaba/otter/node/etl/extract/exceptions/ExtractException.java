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

package com.alibaba.otter.node.etl.extract.exceptions;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * ExtractException for extract module.
 * 
 * @author xiaoqing.zhouxq
 */
public class ExtractException extends NestableRuntimeException {

    private static final long serialVersionUID = 2680820522662343759L;
    private String            errorCode;
    private String            errorDesc;

    public ExtractException(String errorCode){
        super(errorCode);
    }

    public ExtractException(String errorCode, Throwable cause){
        super(errorCode, cause);
    }

    public ExtractException(String errorCode, String errorDesc){
        super(errorCode + ":" + errorDesc);
    }

    public ExtractException(String errorCode, String errorDesc, Throwable cause){
        super(errorCode + ":" + errorDesc, cause);
    }

    public ExtractException(Throwable cause){
        super(cause);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
