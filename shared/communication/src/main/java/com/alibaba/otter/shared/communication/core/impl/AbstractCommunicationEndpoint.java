package com.alibaba.otter.shared.communication.core.impl;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.alibaba.otter.shared.communication.core.CommunicationEndpoint;
import com.alibaba.otter.shared.communication.core.CommunicationRegistry;
import com.alibaba.otter.shared.communication.core.exception.CommunicationException;
import com.alibaba.otter.shared.communication.core.model.Event;
import com.alibaba.otter.shared.communication.core.model.heart.HeartEvent;

/**
 * 默认的endpoint实现
 * 
 * @author jianghang 2011-9-9 下午07:01:49
 */
public abstract class AbstractCommunicationEndpoint implements CommunicationEndpoint {

    // 需要禁止输出详细内容的事件
    private static final Logger logger         = LoggerFactory.getLogger(CommunicationEndpoint.class);

    private static final String DEFAULT_METHOD = "handleEvent";

    /**
     * 处理指定的事件
     */
    public Object acceptEvent(Event event) {
        if (event instanceof HeartEvent) {
            return event; // 针对心跳请求，返回一个随意结果
        }

        try {
            Object action = CommunicationRegistry.getAction(event.getType());
            if (action != null) {

                // 通过反射获取方法并执行
                String methodName = "on" + StringUtils.capitalize(event.getType().toString());
                Method method = ReflectionUtils.findMethod(action.getClass(), methodName,
                                                           new Class[] { event.getClass() });
                if (method == null) {
                    methodName = DEFAULT_METHOD; // 尝试一下默认方法
                    method = ReflectionUtils.findMethod(action.getClass(), methodName, new Class[] { event.getClass() });

                    if (method == null) { // 再尝试一下Event参数
                        method = ReflectionUtils.findMethod(action.getClass(), methodName, new Class[] { Event.class });
                    }
                }
                // 方法不为空就调用指定的方法,反之调用缺省的处理函数
                if (method != null) {
                    try {
                        ReflectionUtils.makeAccessible(method);
                        return method.invoke(action, new Object[] { event });
                    } catch (Throwable e) {
                        throw new CommunicationException("method_invoke_error:" + methodName, e);
                    }
                } else {
                    throw new CommunicationException("no_method_error for["
                                                     + StringUtils.capitalize(event.getType().toString())
                                                     + "] in Class[" + action.getClass().getName() + "]");
                }

            }

            throw new CommunicationException("eventType_no_action", event.getType().name());
        } catch (RuntimeException e) {
            logger.error("endpoint_error", e);
            throw e;
        } catch (Exception e) {
            logger.error("endpoint_error", e);
            throw new CommunicationException(e);
        }
    }
}
