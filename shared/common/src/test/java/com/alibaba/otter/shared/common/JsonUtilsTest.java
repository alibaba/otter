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

package com.alibaba.otter.shared.common;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.JSONWriter;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.otter.shared.common.model.config.Transient;
import com.alibaba.otter.shared.common.model.config.data.DataMediaType;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.common.utils.ByteUtils;
import com.alibaba.otter.shared.common.utils.JsonUtils;

public class JsonUtilsTest extends BaseOtterTest {

    @Test
    public void test_filter() {
        DbMediaSource media = new DbMediaSource();
        media.setGmtCreate(new Date());
        media.setGmtModified(new Date());
        media.setId(1L);
        media.setName("test");
        media.setType(DataMediaType.MYSQL);// 这个是枚举值
        media.setUsername("ljh");
        media.setPassword("ljh");

        String str = JsonUtils.marshalToString(media, "gmtCreate", "gmtModified", "id", "name", "type");
        want.string(str).notContain("id");

        DbMediaSource target = JsonUtils.unmarshalFromString(str, DbMediaSource.class);
        want.object(target.getUsername()).notNull();
        want.object(target.getPassword()).notNull();
    }

    @Test
    public void test_map_byte() {
        Map data = new HashMap<String, byte[]>();
        byte[] one = new byte[] { 1, 2, 3 };
        byte[] two = new byte[] { 4, 5, 6 };
        data.put("one", one);
        data.put("two", two);

        byte[] bytes = JsonUtils.marshalToByte(data);
        Map target = JsonUtils.unmarshalFromByte(bytes, Map.class);
        byte[] oneDates = ByteUtils.base64StringToBytes((String) target.get("one"));
        byte[] twoDates = ByteUtils.base64StringToBytes((String) target.get("two"));

        check(oneDates, one);
        check(twoDates, two);
    }

    @Test
    public void test_bytes() {
        Map data = new HashMap<String, byte[]>();
        byte[] one = new byte[] { 1, 2, 3 };
        byte[] two = new byte[] { 4, 5, 6 };
        data.put("one", one);
        data.put("two", two);

        StringWriter jsonStr = new StringWriter();
        JSONWriter writer = new JSONWriter(jsonStr);//超大文本写入
        writer.startArray();
        writer.writeValue(one);
        writer.writeValue(two);
        writer.endArray();
        try {
            writer.close();
        } catch (IOException e) {
        }

        JSONReader reader = new JSONReader(new StringReader(jsonStr.getBuffer().toString()));
        byte[] oneDates = null;
        byte[] twoDates = null;
        reader.startArray();
        while (reader.hasNext()) {
            if (oneDates == null) {
                oneDates = reader.readObject(byte[].class);
            } else if (twoDates == null) {
                twoDates = reader.readObject(byte[].class);
            } else {
                want.fail("not possible");
            }

        }
        reader.endArray();
        reader.close();
        check(oneDates, one);
        check(twoDates, two);
    }

    private void check(byte[] src, byte[] dest) {
        want.object(src).notNull();
        want.object(dest).notNull();
        want.bool(src.length == dest.length).is(true);

        for (int i = 0; i < src.length; i++) {
            if (src[i] != dest[i]) {
                want.fail();
            }
        }
    }

    @Test
    public void test_list() {
        SubPipeKey key1 = new SubPipeKey();
        key1.setId(1);
        key1.setUrl("test1");
        key1.setName("key1");

        SubPipeKey key2 = new SubPipeKey();
        key2.setId(2);
        key2.setUrl("test2");
        key2.setName("key1");

        PipeEventData<List<PipeKey>> data = new PipeEventData<List<PipeKey>>();
        data.setId(1);
        data.setKey(key1);
        String json = JsonUtils.marshalToString(data, SerializerFeature.WriteClassName);
        System.out.println(json);

        PipeEventData result = JsonUtils.unmarshalFromString(json, new TypeReference<PipeEventData<List<PipeKey>>>() {

        });
        System.out.println(result);
        want.bool(result.getKey() instanceof SubPipeKey).is(true);
    }

    @Test
    public void testTransTransient() {
        SubPipeKey key = new SubPipeKey();
        key.setName("hello");
        key.setId(1);

        String json = JsonUtils.marshalToStringWithoutTransient(key);
        System.out.println(json);
    }

    public static class PipeEventData<T> {

        @JSONField(serialzeFeatures = { SerializerFeature.WriteClassName })
        private PipeKey key;

        @JSONField(serialize = false, deserialize = true)
        private int     id;

        public PipeKey getKey() {
            return key;
        }

        public void setKey(PipeKey key) {
            this.key = key;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

    }

    public static class PipeKey {

        private String url;
        private int    id;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

    }

    public static class SubPipeKey extends PipeKey {

        @Transient
        private String name = "l";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }
}
