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

package com.alibaba.otter.manager.web.home.module.screen.api;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.manager.web.common.api.JsonResult;

/**
 * @author zebin.xuzb @ 2012-5-18
 */
public class AbstractJsonScreen<DATA> {

    protected static final Logger log = LoggerFactory.getLogger(AbstractJsonScreen.class);

    @Autowired
    private HttpServletResponse   response;

    protected void returnError(String errMessage) {
        JsonResult result = new JsonResult(false);
        result.setErrMessage(errMessage);
        String content = buildJson(result);
        writeResponse(content);
    }

    protected void returnSuccess() {
        JsonResult result = new JsonResult(true);
        String content = buildJson(result);
        writeResponse(content);

    }

    protected void returnSuccess(DATA data) {
        JsonResult result = new JsonResult(true);
        result.setData(data);
        String content = buildJson(result);
        writeResponse(content);

    }

    protected String buildJson(JsonResult result) {
        return JSON.toJSONString(result);
    }

    private void writeResponse(String content) {
        try {
            response.getWriter().write(content);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
