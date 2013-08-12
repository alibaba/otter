package com.alibaba.otter.shared.communication.app.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * @author jianghang 2011-9-13 下午08:31:36
 */
public class AppUpdateEvent extends Event {

    private static final long serialVersionUID = 810191575813164952L;

    public AppUpdateEvent(){
        super(AppEventType.update);
    }

    private String     name;
    private BigInteger bigIntegerValue;
    private BigDecimal bigDecimalValue;
    private UpdateData data;

    public static class UpdateData implements Serializable {

        private static final long serialVersionUID = -2591770066519646446L;
        private String            name;
        private BigInteger        bigIntegerValue;
        private BigDecimal        bigDecimalValue;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigInteger getBigIntegerValue() {
            return bigIntegerValue;
        }

        public void setBigIntegerValue(BigInteger bigIntegerValue) {
            this.bigIntegerValue = bigIntegerValue;
        }

        public BigDecimal getBigDecimalValue() {
            return bigDecimalValue;
        }

        public void setBigDecimalValue(BigDecimal bigDecimalValue) {
            this.bigDecimalValue = bigDecimalValue;
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigInteger getBigIntegerValue() {
        return bigIntegerValue;
    }

    public void setBigIntegerValue(BigInteger bigIntegerValue) {
        this.bigIntegerValue = bigIntegerValue;
    }

    public BigDecimal getBigDecimalValue() {
        return bigDecimalValue;
    }

    public void setBigDecimalValue(BigDecimal bigDecimalValue) {
        this.bigDecimalValue = bigDecimalValue;
    }

    public UpdateData getData() {
        return data;
    }

    public void setData(UpdateData data) {
        this.data = data;
    }

}
