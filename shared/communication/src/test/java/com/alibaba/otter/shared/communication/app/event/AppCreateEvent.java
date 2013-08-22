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

package com.alibaba.otter.shared.communication.app.event;

import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * @author jianghang 2011-9-13 下午08:31:36
 */
public class AppCreateEvent extends Event {

    private static final long serialVersionUID = 810191575813164952L;

    public AppCreateEvent(){
        super(AppEventType.create);
    }

    private String    name;
    private int       intValue;
    private boolean   boolValue;
    private float     floatValue;
    private double    doubleValue;
    private long      longValue;
    private char      charValue;
    private byte      byteValue;
    private short     shortValue;
    private Integer   integerValue;
    private Boolean   boolObjValue;
    private Float     floatObjValue;
    private Double    doubleObjValue;
    private Long      longObjValue;
    private Character characterValue;
    private Short     shortObjValue;
    private Byte      byteObjValue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public boolean isBoolValue() {
        return boolValue;
    }

    public void setBoolValue(boolean boolValue) {
        this.boolValue = boolValue;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(float floatValue) {
        this.floatValue = floatValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }

    public char getCharValue() {
        return charValue;
    }

    public void setCharValue(char charValue) {
        this.charValue = charValue;
    }

    public byte getByteValue() {
        return byteValue;
    }

    public void setByteValue(byte byteValue) {
        this.byteValue = byteValue;
    }

    public short getShortValue() {
        return shortValue;
    }

    public void setShortValue(short shortValue) {
        this.shortValue = shortValue;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(Integer integerValue) {
        this.integerValue = integerValue;
    }

    public Boolean getBoolObjValue() {
        return boolObjValue;
    }

    public void setBoolObjValue(Boolean boolObjValue) {
        this.boolObjValue = boolObjValue;
    }

    public Float getFloatObjValue() {
        return floatObjValue;
    }

    public void setFloatObjValue(Float floatObjValue) {
        this.floatObjValue = floatObjValue;
    }

    public Double getDoubleObjValue() {
        return doubleObjValue;
    }

    public void setDoubleObjValue(Double doubleObjValue) {
        this.doubleObjValue = doubleObjValue;
    }

    public Long getLongObjValue() {
        return longObjValue;
    }

    public void setLongObjValue(Long longObjValue) {
        this.longObjValue = longObjValue;
    }

    public Character getCharacterValue() {
        return characterValue;
    }

    public void setCharacterValue(Character characterValue) {
        this.characterValue = characterValue;
    }

    public Short getShortObjValue() {
        return shortObjValue;
    }

    public void setShortObjValue(Short shortObjValue) {
        this.shortObjValue = shortObjValue;
    }

    public Byte getByteObjValue() {
        return byteObjValue;
    }

    public void setByteObjValue(Byte byteObjValue) {
        this.byteObjValue = byteObjValue;
    }

}
