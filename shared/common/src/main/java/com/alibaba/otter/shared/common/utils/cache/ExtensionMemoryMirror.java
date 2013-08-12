package com.alibaba.otter.shared.common.utils.cache;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.MapMaker;

/**
 * 简单内存镜像实现
 * 
 * <pre>
 * 1. 使用hashMap做为存储
 * 2. 支持时间戳作为版本号
 * 3. 支持ComputeFunction，在get结果为null允许进行回调处理
 * </pre>
 */
public class ExtensionMemoryMirror<KEY, VALUE> {

    private final Map<KEY, VALUE>             store;
    private final ComputeFunction<KEY, VALUE> function;

    public ExtensionMemoryMirror(ComputeFunction<KEY, VALUE> function){
        this.function = function;
        store = new MapMaker().makeMap();
    }

    public synchronized VALUE get(KEY key) {
        if (store.containsKey(key)) {
            KEY oldKey = null;
            for (KEY k : store.keySet()) {
                if (k.equals(key)) {
                    oldKey = k;
                    break;
                }
            }

            if (((Comparable) key).compareTo(oldKey) > 0) {
                VALUE result = function.apply(key);
                // 一定要先删除key，否则put时key不会被更新，对应的时间戳一直为老对象
                remove(key);
                put(key, result);
                return result;
            } else {
                return store.get(key);
            }
        } else {
            VALUE result = function.apply(key);
            remove(key);
            put(key, result);
            return result;
        }

    }

    public synchronized void put(KEY key, VALUE value) {
        store.put(getKey(key), value);
    }

    public synchronized void remove(Object key) {
        store.remove(getKey(key));
    }

    public synchronized void clear() {
        store.clear();
    }

    public Iterator getKeys() {
        return store.keySet().iterator();
    }

    public Set<Entry<KEY, VALUE>> entrySet() {
        return store.entrySet();
    }

    public int size() {
        return store.size();
    }

    private KEY getKey(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key not be null");
        }

        return (KEY) key;
    }

    public static interface ComputeFunction<KEY, VALUE> {

        VALUE apply(KEY key);

    }

}
