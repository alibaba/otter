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

public final class Assert {

    public static <T> T assertNotNull(T object) {
        return assertNotNull(object, null, null, (Object[]) null);
    }

    /** 确保对象不为空，否则抛出<code>IllegalArgumentException</code>。 */
    public static <T> T assertNotNull(T object, String message, Object... args) {
        return assertNotNull(object, null, message, args);
    }

    /** 确保对象不为空，否则抛出指定异常，默认为<code>IllegalArgumentException</code>。 */
    public static <T> T assertNotNull(T object, ExceptionType exceptionType, String message, Object... args) {
        if (object == null) {
            if (exceptionType == null) {
                exceptionType = ExceptionType.ILLEGAL_ARGUMENT;
            }

            throw exceptionType.newInstance(getMessage(message, args,
                                                       "[Assertion failed] - the argument is required; it must not be null"));
        }

        return object;
    }

    /** 确保对象为空，否则抛出<code>IllegalArgumentException</code>。 */
    public static <T> T assertNull(T object) {
        return assertNull(object, null, null, (Object[]) null);
    }

    /** 确保对象为空，否则抛出<code>IllegalArgumentException</code>。 */
    public static <T> T assertNull(T object, String message, Object... args) {
        return assertNull(object, null, message, args);
    }

    /** 确保对象为空，否则抛出指定异常，默认为<code>IllegalArgumentException</code>。 */
    public static <T> T assertNull(T object, ExceptionType exceptionType, String message, Object... args) {
        if (object != null) {
            if (exceptionType == null) {
                exceptionType = ExceptionType.ILLEGAL_ARGUMENT;
            }

            throw exceptionType.newInstance(getMessage(message, args,
                                                       "[Assertion failed] - the object argument must be null"));
        }

        return object;
    }

    /** 确保表达式为真，否则抛出<code>IllegalArgumentException</code>。 */
    public static void assertTrue(boolean expression) {
        assertTrue(expression, null, null, (Object[]) null);
    }

    /** 确保表达式为真，否则抛出<code>IllegalArgumentException</code>。 */
    public static void assertTrue(boolean expression, String message, Object... args) {
        assertTrue(expression, null, message, args);
    }

    /** 确保表达式为真，否则抛出指定异常，默认为<code>IllegalArgumentException</code>。 */
    public static void assertTrue(boolean expression, ExceptionType exceptionType, String message, Object... args) {
        if (!expression) {
            if (exceptionType == null) {
                exceptionType = ExceptionType.ILLEGAL_ARGUMENT;
            }

            throw exceptionType.newInstance(getMessage(message, args,
                                                       "[Assertion failed] - the expression must be true"));
        }
    }

    /** 取得带参数的消息。 */
    private static String getMessage(String message, Object[] args, String defaultMessage) {
        if (message == null) {
            message = defaultMessage;
        }

        if (args == null || args.length == 0) {
            return message;
        }

        return String.format(message, args);
    }

    /** Assertion错误类型。 */
    public static enum ExceptionType {
        ILLEGAL_ARGUMENT {

            @Override
            RuntimeException newInstance(String message) {
                return new IllegalArgumentException(message);
            }
        };

        abstract RuntimeException newInstance(String message);
    }
}
