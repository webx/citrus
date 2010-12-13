/*
 * Copyright 2010 Alibaba Group Holding Limited.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.alibaba.citrus.util;

import static com.alibaba.citrus.util.Assert.ExceptionType.*;

/**
 * 断言工具，用来实现<a
 * href="http://martinfowler.com/ieeeSoftware/failFast.pdf">Fail-Fast</a>。
 * <p>
 * <strong>注意事项：</strong>
 * </p>
 * <ul>
 * <li>Assertion是用来明确设置程序中的条件和约定的，它是一种程序员之间交流的工具，而不是和最终用户交流的工具。</li>
 * <li>一个经过完整单元测试的正确程序，不应该使任何一条assertion失败。</li>
 * <li>应避免过于复杂的assertion条件，而占用过多的运行时间。除非Assertion失败。</li>
 * <li>Assertion的出错信息不是给最终用户看的，因此没必要写得过于详细，更没必要考虑国际化的问题，以至于浪费CPU的时间。
 * 例如下面的语句是要避免的：
 * 
 * <pre>
 * assertTrue(type instanceof MyType, &quot;Unsupported type: &quot; + type);
 * </pre>
 * 
 * 可以替换成：
 * 
 * <pre>
 * assertTrue(type instanceof MyType, &quot;Unsupported type: %s&quot;, type);
 * </pre>
 * 
 * 这样，当assertion顺利通过时，不会占用CPU时间。</li>
 * </ul>
 * <p>
 * 此外，部分方法具有返回值，以方便编程，例如：
 * </p>
 * 
 * <pre>
 * void foo(String param) {
 *     bar(assertNotNull(param));
 * }
 * 
 * int bar(String param) {
 *     if (true) {
 *         ...
 *     }
 *     
 *     return unreachableCode(); 
 * }
 * </pre>
 * 
 * @author Michael Zhou
 */
public final class Assert {
    /**
     * 确保对象不为空，否则抛出<code>IllegalArgumentException</code>。
     */
    public static <T> T assertNotNull(T object) {
        return assertNotNull(object, null, null, (Object[]) null);
    }

    /**
     * 确保对象不为空，否则抛出<code>IllegalArgumentException</code>。
     */
    public static <T> T assertNotNull(T object, String message, Object... args) {
        return assertNotNull(object, null, message, args);
    }

    /**
     * 确保对象不为空，否则抛出指定异常，默认为<code>IllegalArgumentException</code>。
     */
    public static <T> T assertNotNull(T object, ExceptionType exceptionType, String message, Object... args) {
        if (object == null) {
            if (exceptionType == null) {
                exceptionType = ILLEGAL_ARGUMENT;
            }

            throw exceptionType.newInstance(getMessage(message, args,
                    "[Assertion failed] - the argument is required; it must not be null"));
        }

        return object;
    }

    /**
     * 确保对象为空，否则抛出<code>IllegalArgumentException</code>。
     */
    public static <T> T assertNull(T object) {
        return assertNull(object, null, null, (Object[]) null);
    }

    /**
     * 确保对象为空，否则抛出<code>IllegalArgumentException</code>。
     */
    public static <T> T assertNull(T object, String message, Object... args) {
        return assertNull(object, null, message, args);
    }

    /**
     * 确保对象为空，否则抛出指定异常，默认为<code>IllegalArgumentException</code>。
     */
    public static <T> T assertNull(T object, ExceptionType exceptionType, String message, Object... args) {
        if (object != null) {
            if (exceptionType == null) {
                exceptionType = ILLEGAL_ARGUMENT;
            }

            throw exceptionType.newInstance(getMessage(message, args,
                    "[Assertion failed] - the object argument must be null"));
        }

        return object;
    }

    /**
     * 确保表达式为真，否则抛出<code>IllegalArgumentException</code>。
     */
    public static void assertTrue(boolean expression) {
        assertTrue(expression, null, null, (Object[]) null);
    }

    /**
     * 确保表达式为真，否则抛出<code>IllegalArgumentException</code>。
     */
    public static void assertTrue(boolean expression, String message, Object... args) {
        assertTrue(expression, null, message, args);
    }

    /**
     * 确保表达式为真，否则抛出指定异常，默认为<code>IllegalArgumentException</code>。
     */
    public static void assertTrue(boolean expression, ExceptionType exceptionType, String message, Object... args) {
        if (!expression) {
            if (exceptionType == null) {
                exceptionType = ILLEGAL_ARGUMENT;
            }

            throw exceptionType.newInstance(getMessage(message, args,
                    "[Assertion failed] - the expression must be true"));
        }
    }

    /**
     * 不可能到达的代码。
     */
    public static <T> T unreachableCode() {
        unreachableCode(null, (Object[]) null);
        return null;
    }

    /**
     * 不可能到达的代码。
     */
    public static <T> T unreachableCode(String message, Object... args) {
        throw UNREACHABLE_CODE.newInstance(getMessage(message, args,
                "[Assertion failed] - the code is expected as unreachable"));
    }

    /**
     * 不可能发生的异常。
     */
    public static <T> T unexpectedException(Throwable e) {
        unexpectedException(e, null, (Object[]) null);
        return null;
    }

    /**
     * 不可能发生的异常。
     */
    public static <T> T unexpectedException(Throwable e, String message, Object... args) {
        RuntimeException exception = UNEXPECTED_FAILURE.newInstance(getMessage(message, args,
                "[Assertion failed] - unexpected exception is thrown"));

        exception.initCause(e);

        throw exception;
    }

    /**
     * 未预料的失败。
     */
    public static <T> T fail() {
        fail(null, (Object[]) null);
        return null;
    }

    /**
     * 未预料的失败。
     */
    public static <T> T fail(String message, Object... args) {
        throw UNEXPECTED_FAILURE.newInstance(getMessage(message, args, "[Assertion failed] - unexpected failure"));
    }

    /**
     * 不支持的操作。
     */
    public static <T> T unsupportedOperation() {
        unsupportedOperation(null, (Object[]) null);
        return null;
    }

    /**
     * 不支持的操作。
     */
    public static <T> T unsupportedOperation(String message, Object... args) {
        throw UNSUPPORTED_OPERATION.newInstance(getMessage(message, args,
                "[Assertion failed] - unsupported operation or unimplemented function"));
    }

    /**
     * 取得带参数的消息。
     */
    private static String getMessage(String message, Object[] args, String defaultMessage) {
        if (message == null) {
            message = defaultMessage;
        }

        if (args == null || args.length == 0) {
            return message;
        }

        return String.format(message, args);
    }

    /**
     * Assertion错误类型。
     */
    public static enum ExceptionType {
        ILLEGAL_ARGUMENT {
            @Override
            RuntimeException newInstance(String message) {
                return new IllegalArgumentException(message);
            }
        },

        ILLEGAL_STATE {
            @Override
            RuntimeException newInstance(String message) {
                return new IllegalStateException(message);
            }
        },

        NULL_POINT {
            @Override
            RuntimeException newInstance(String message) {
                return new NullPointerException(message);
            }
        },

        UNREACHABLE_CODE {
            @Override
            RuntimeException newInstance(String message) {
                return new UnreachableCodeException(message);
            }
        },

        UNEXPECTED_FAILURE {
            @Override
            RuntimeException newInstance(String message) {
                return new UnexpectedFailureException(message);
            }
        },

        UNSUPPORTED_OPERATION {
            @Override
            RuntimeException newInstance(String message) {
                return new UnsupportedOperationException(message);
            }
        };

        abstract RuntimeException newInstance(String message);
    }
}
