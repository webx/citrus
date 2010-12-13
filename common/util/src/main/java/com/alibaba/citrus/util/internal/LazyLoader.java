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
package com.alibaba.citrus.util.internal;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ClassUtil.*;

/**
 * 延迟加载对象。
 * <p>
 * 目前有三种可用的加载方案，各方案的性能因JDK和环境而异：
 * </p>
 * <ol>
 * <li>同步的(Synchronized) － 最慢，但最经典可靠。</li>
 * <li>基于线程的(PerThread) － 比第一种快5－50倍左右，理论上也是可靠的。</li>
 * <li>基于DCL的(Double-Checked Locking) － 比第一种快5－70倍左右，理论上不可靠，但在JDK5以后应该没有问题了。</li>
 * </ol>
 * 
 * @author Michael Zhou
 */
public abstract class LazyLoader<T, C> {
    private final Loader<T, C> loader;

    protected LazyLoader(Loader<T, C> loader) {
        this.loader = assertNotNull(loader);
    }

    public final T getInstance() {
        return getInstance(null);
    }

    public abstract T getInstance(C context);

    public abstract boolean testInstance();

    /**
     * 调用loader装载对象。
     */
    protected final T load(C context) {
        try {
            return loader.load(context);
        } catch (RuntimeException e) {
            if (loader instanceof ExceptionHandler<?, ?>) {
                return ((ExceptionHandler<T, C>) loader).handle(e, context);
            } else {
                throw e;
            }
        }
    }

    /**
     * 用来创建对象实例。
     */
    public static interface Loader<T, C> {
        T load(C context);
    }

    public static interface ExceptionHandler<T, C> extends Loader<T, C> {
        T handle(RuntimeException e, C context);
    }

    /**
     * 取得默认的方案。
     */
    public static <T, C> LazyLoader<T, C> getDefault(Loader<T, C> loader) {
        return getDoubleCheckedLockingLazyLoader(loader);
    }

    /**
     * 用保守的同步方法来创建对象。
     * <p>
     * 该方案在任何JVM中都是安全的。
     * </p>
     */
    public static <T, C> LazyLoader<T, C> getSynchronizedLazyLoader(Loader<T, C> loader) {
        return new LazyLoader<T, C>(loader) {
            private boolean loaded;
            private T instance;

            @Override
            public T getInstance(C context) {
                synchronized (this) {
                    if (!loaded) {
                        instance = load(context);
                        loaded = true;
                    }
                }

                return instance;
            }

            @Override
            public synchronized boolean testInstance() {
                return loaded;
            }
        };
    }

    /**
     * 利用<code>ThreadLocal</code>来标记当前线程是否完成了同步操作，从而实现延迟装载。
     * <p>
     * 该方案在任何JVM中都是安全的。
     * </p>
     */
    public static <T, C> LazyLoader<T, C> getPerThreadLazyLoader(Loader<T, C> loader) {
        return new LazyLoader<T, C>(loader) {
            private final ThreadLocal<Boolean> synced = new ThreadLocal<Boolean>();
            private boolean loaded;
            private T instance;

            @Override
            public T getInstance(C context) {
                if (synced.get() == null) {
                    synchronized (this) {
                        if (!loaded) {
                            instance = load(context);
                            loaded = true;
                        }
                    }

                    synced.set(Boolean.TRUE);
                }

                return instance;
            }

            @Override
            public boolean testInstance() {
                if (synced.get() == null) {
                    synchronized (this) {
                        if (loaded) {
                            synced.set(Boolean.TRUE);
                        }

                        return loaded;
                    }
                } else {
                    return true;
                }
            }
        };
    }

    /**
     * 利用<code>volatile</code>变量的特性，以DCL的方式进行装载。
     * <p>
     * 注意，该实现理论上比<code>SynchronizedLazyLoader</code>有更好的性能，但其正确性取决于JVM的实现。<br>
     * 一般认为，JDK5以后，支持对<code>volatile</code>变量的DCL操作。
     * </p>
     */
    public static <T, C> LazyLoader<T, C> getDoubleCheckedLockingLazyLoader(Loader<T, C> loader) {
        return new LazyLoader<T, C>(loader) {
            private volatile boolean loaded;
            private volatile T instance;

            @Override
            public T getInstance(C context) {
                if (!loaded) {
                    synchronized (this) {
                        if (!loaded) {
                            instance = load(context);
                            loaded = true;
                        }
                    }
                }

                return instance;
            }

            @Override
            public boolean testInstance() {
                return loaded;
            }
        };
    }

    /**
     * 转换成字符串表示。
     */
    @Override
    public String toString() {
        return String.format("LazyLoader(%s%s)", getSimpleClassName(loader.getClass()), (testInstance() ? ", loaded"
                : EMPTY_STRING));
    }
}
