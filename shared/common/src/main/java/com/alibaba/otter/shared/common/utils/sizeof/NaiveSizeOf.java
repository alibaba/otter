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

package com.alibaba.otter.shared.common.utils.sizeof;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/* http://www.glenmccl.com/tip_038.htm modified 1999-10 */
/**
 * Sizeof For Java(tm) Java(tm) has no sizeof() operator like C/C++.With uniform sizes for primitive data types, and a
 * different style of memory allocation, the need for sizeof() really isn't there. And it's hard to define what sizeof()
 * would mean anyway, given that an object may not contain other objects, but only references to them. But it's
 * interesting to experiment with the 1.1 reflection feature and see whether a method can be devised that will return
 * useful information about object sizes. The Sizeof class below tries to do this, for a passed-in data structure. It
 * walks the structure and tallies up the total size in bytes. It ignores alignment and packing issues and hidden fields
 * in structures, and assumes a boolean is of size 1 and a reference of size 4 (reference sizes may vary; for example
 * SZ_REF might be 8 on a machine with 64-bit pointers). It does not count static data members of class instances, but
 * does include members inherited/implemented from superclasses and interfaces. It does not follow references in object
 * instances or in arrays, except for the case of a multi-dimensional array, where the reference is to another array.
 */
public class NaiveSizeOf {

    private NaiveSizeOf(){
        super();
    }

    private static final int SZ_REF = 4;

    public static int sizeof(boolean b) {
        return 1;
    }

    public static int sizeof(byte b) {
        return 1;
    }

    public static int sizeof(char c) {
        return 2;
    }

    public static int sizeof(short s) {
        return 2;
    }

    public static int sizeof(int i) {
        return 4;
    }

    public static int sizeof(long l) {
        return 8;
    }

    public static int sizeof(float f) {
        return 4;
    }

    public static int sizeof(double d) {
        return 8;
    }

    private static int size_inst(final Class c) {
        Field flds[] = c.getDeclaredFields();
        int sz = 0;

        for (int i = 0; i < flds.length; ++i) {
            Field f = flds[i];
            if (!c.isInterface() && (f.getModifiers() & Modifier.STATIC) != 0) continue;
            sz += size_prim(f.getType());
        }

        if (c.getSuperclass() != null) sz += size_inst(c.getSuperclass());

        Class cv[] = c.getInterfaces();
        for (int i = 0; i < cv.length; ++i)
            sz += size_inst(cv[i]);

        return sz;
    }

    private static int size_prim(final Class t) {
        if (t == Boolean.TYPE) return 1;
        else if (t == Byte.TYPE) return 1;
        else if (t == Character.TYPE) return 2;
        else if (t == Short.TYPE) return 2;
        else if (t == Integer.TYPE) return 4;
        else if (t == Long.TYPE) return 8;
        else if (t == Float.TYPE) return 4;
        else if (t == Double.TYPE) return 8;
        else if (t == Void.TYPE) return 0;
        else return SZ_REF;
    }

    private static int size_arr(final Object obj, final Class c) {
        Class ct = c.getComponentType();
        int len = Array.getLength(obj);

        if (ct.isPrimitive()) {
            return len * size_prim(ct);
        } else {
            int sz = 0;
            for (int i = 0; i < len; ++i) {
                sz += SZ_REF;
                Object obj2 = Array.get(obj, i);
                if (obj2 == null) continue;
                Class c2 = obj2.getClass();
                if (!c2.isArray()) continue;
                sz += size_arr(obj2, c2);
            }
            return sz;
        }
    }

    public static int sizeof(final Object obj) {
        if (null == obj) return 0;

        Class c = obj.getClass();

        if (c.isArray()) return size_arr(obj, c);
        else return size_inst(c);
    }

}
