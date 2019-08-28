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

package com.alibaba.otter.shared.common.utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.otter.shared.common.model.config.Transient;
import com.alibaba.otter.shared.common.utils.JsonUtils.InetSocketAddressSerializer.InetAddressDeserializer;
import com.alibaba.otter.shared.common.utils.JsonUtils.InetSocketAddressSerializer.InetSocketAddressDeserializer;

/**
 * 字节处理相关工具类
 * 
 * @author jianghang
 */
public class JsonUtils {

    static {
        SerializeConfig.getGlobalInstance().put(InetAddress.class, InetAddressSerializer.instance);
        SerializeConfig.getGlobalInstance().put(Inet4Address.class, InetAddressSerializer.instance);
        SerializeConfig.getGlobalInstance().put(Inet6Address.class, InetAddressSerializer.instance);
        SerializeConfig.getGlobalInstance().put(InetSocketAddress.class, InetSocketAddressSerializer.instance);

        ParserConfig.getGlobalInstance().getDeserializers().put(InetAddress.class, InetAddressDeserializer.instance);
        ParserConfig.getGlobalInstance().getDeserializers().put(Inet4Address.class, InetAddressDeserializer.instance);
        ParserConfig.getGlobalInstance().getDeserializers().put(Inet6Address.class, InetAddressDeserializer.instance);
        ParserConfig.getGlobalInstance()
            .getDeserializers()
            .put(InetSocketAddress.class, InetSocketAddressDeserializer.instance);
        SerializeConfig.getGlobalInstance().put(Inet6Address.class, InetAddressSerializer.instance);

        // ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        ParserConfig.getGlobalInstance().addAccept("com.alibaba.otter.");
        ParserConfig.getGlobalInstance().addAccept("com.taobao.tddl.dbsync.");
    }

    public static <T> T unmarshalFromByte(byte[] bytes, Class<T> targetClass) {
        return (T) JSON.parseObject(bytes, targetClass);// 默认为UTF-8
    }

    public static <T> T unmarshalFromByte(byte[] bytes, TypeReference<T> type) {
        return (T) JSON.parseObject(bytes, type.getType());
    }

    public static byte[] marshalToByte(Object obj) {
        return JSON.toJSONBytes(obj); // 默认为UTF-8
    }

    public static byte[] marshalToByte(Object obj, SerializerFeature... features) {
        return JSON.toJSONBytes(obj, features); // 默认为UTF-8
    }

    public static <T> T unmarshalFromString(String json, Class<T> targetClass) {
        return (T) JSON.parseObject(json, targetClass);// 默认为UTF-8
    }

    public static <T> T unmarshalFromString(String json, TypeReference<T> type) {
        return (T) JSON.parseObject(json, type);// 默认为UTF-8
    }

    public static String marshalToString(Object obj) {
        return JSON.toJSONString(obj); // 默认为UTF-8
    }

    public static String marshalToString(Object obj, SerializerFeature... features) {
        return JSON.toJSONString(obj, features); // 默认为UTF-8
    }

    public static String marshalToStringWithoutTransient(Object obj) {
        // 获取忽略字段
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        List<String> transientFileds = new ArrayList<String>();
        for (Field f : fields) {
            if (!f.isAccessible()) {
                f.setAccessible(true);
            }

            Transient anno = f.getAnnotation(Transient.class);
            if (anno != null && anno.value()) {
                transientFileds.add(f.getName());
            }
        }

        return marshalToString(obj, transientFileds.toArray(new String[transientFileds.size()]));
    }

    /**
     * 可以允许指定一些过滤字段进行生成json对象
     */
    public static String marshalToString(Object obj, String... fliterFields) {
        final List<String> propertyFliters = Arrays.asList(fliterFields);
        SerializeWriter out = new SerializeWriter();
        try {
            JSONSerializer serializer = new JSONSerializer(out);
            serializer.getPropertyFilters().add(new PropertyFilter() {

                public boolean apply(Object source, String name, Object value) {
                    return !propertyFliters.contains(name);
                }

            });
            serializer.write(obj);
            return out.toString();
        } finally {
            out.close();
        }
    }

    public static class InetAddressSerializer implements ObjectSerializer {

        public static InetAddressSerializer instance = new InetAddressSerializer();

        public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType)
                                                                                                     throws IOException {
            if (object == null) {
                serializer.writeNull();
                return;
            }

            InetAddress address = (InetAddress) object;
            String[] data = StringUtils.split(address.toString(), '/');
            serializer.write(data[0]);
        }

        public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
                                                                                                                   throws IOException {
            if (object == null) {
                serializer.writeNull();
                return;
            }

            InetAddress address = (InetAddress) object;
            String[] data = StringUtils.split(address.toString(), '/');
            serializer.write(data[0]);
        }
    }

    public static class InetSocketAddressSerializer implements ObjectSerializer {

        public static InetSocketAddressSerializer instance = new InetSocketAddressSerializer();

        public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType)
                                                                                                     throws IOException {
            if (object == null) {
                serializer.writeNull();
                return;
            }

            SerializeWriter out = serializer.out;
            InetSocketAddress address = (InetSocketAddress) object;
            InetAddress inetAddress = address.getAddress();
            serializer.write('{');
            out.writeFieldName("address");
            if (inetAddress != null) {
                serializer.write(inetAddress);
            } else {
                out.writeString(address.getHostString());
            }
            out.write(',');
            out.writeFieldName("port");
            out.writeInt(address.getPort());
            serializer.write('}');
        }

        public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
                                                                                                                   throws IOException {
            if (object == null) {
                serializer.writeNull();
                return;
            }

            SerializeWriter out = serializer.out;
            InetSocketAddress address = (InetSocketAddress) object;
            address.getHostString();
            InetAddress inetAddress = address.getAddress();
            out.write('{');
            out.writeFieldName("address");
            if (inetAddress != null) {
                serializer.write(inetAddress);
            } else {
                out.writeString(address.getHostString());
            }
            out.write(',');
            out.writeFieldName("port");
            out.writeInt(address.getPort());
            out.write('}');
        }

        public static class InetAddressDeserializer implements ObjectDeserializer {

            public static InetAddressDeserializer instance = new InetAddressDeserializer();

            @Override
            public String deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
                return (String) parser.parse();
            }

            @Override
            public int getFastMatchToken() {
                return 0;
            }

        }

        public static class InetSocketAddressDeserializer implements ObjectDeserializer {

            public static InetSocketAddressDeserializer instance = new InetSocketAddressDeserializer();

            @Override
            public InetSocketAddress deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
                JSONLexer lexer = parser.lexer;
                if (lexer.token() == JSONToken.NULL) {
                    lexer.nextToken();
                    return null;
                }

                parser.accept(JSONToken.LBRACE);

                Object address = null;
                int port = 0;
                for (;;) {
                    String key = lexer.stringVal();
                    lexer.nextToken(JSONToken.COLON);

                    if (key.equals("address")) {
                        parser.accept(JSONToken.COLON);
                        address = parser.parseObject(InetAddress.class);
                    } else if (key.equals("port")) {
                        parser.accept(JSONToken.COLON);
                        if (lexer.token() != JSONToken.LITERAL_INT) {
                            throw new RuntimeException("port is not int");
                        }
                        port = lexer.intValue();
                        lexer.nextToken();
                    } else {
                        parser.accept(JSONToken.COLON);
                        parser.parse();
                    }

                    if (lexer.token() == JSONToken.COMMA) {
                        lexer.nextToken();
                        continue;
                    }

                    break;
                }

                parser.accept(JSONToken.RBRACE);
                return new InetSocketAddress(address.toString(), port);
            }

            @Override
            public int getFastMatchToken() {
                return 0;
            }

        }
    }
}
