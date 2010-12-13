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

import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.alibaba.citrus.util.internal.LazyLoader.ExceptionHandler;
import com.alibaba.citrus.util.internal.LazyLoader.Loader;

/**
 * 测试<code>LazyLoader</code>。
 * 
 * @author Michael Zhou
 */
public class LazyLoaderTests {
    private final static int CONCURRENCY = 100;
    private final static int LOOP = 1000;
    private final static int PERFORMANCE_LOOP = 100000;

    private LazyLoader<Resource, Object> lazyLoader;
    private Throwable failed;

    @Test
    public void performance() throws Exception {
        PerformanceResult[] sum = new PerformanceResult[3];

        sum[0] = performance_test(CONCURRENCY, PERFORMANCE_LOOP, LoaderType.SYNC_LOADER);
        sum[1] = performance_test(CONCURRENCY, PERFORMANCE_LOOP, LoaderType.PERTHREAD_LOADER);
        sum[2] = performance_test(CONCURRENCY, PERFORMANCE_LOOP, LoaderType.DCL_LOADER);

        Arrays.sort(sum);

        System.out.println("Performance Summary:");
        System.out.printf("  %s > %s > %s\n", sum[0], sum[1], sum[2]);

        for (int i = 0; i < sum.length - 1; i++) {
            for (int j = 1; j < sum.length; j++) {
                if (i != j) {
                    System.out.printf("  %s is %.2f times faster than %s\n", sum[i].name,
                            (sum[j].totalTime / (double) sum[i].totalTime), sum[j].name);
                }
            }
        }

        System.out.println("------------------------------------");
    }

    private PerformanceResult performance_test(final int concurrency, final int loop, final LoaderType loaderType)
            throws InterruptedException {
        final long[] start = new long[1];
        Thread[] threads = new Thread[concurrency];
        lazyLoader = loaderType.getLoader();
        final CyclicBarrier startBarrier = new CyclicBarrier(concurrency, new Runnable() {
            public void run() {
                start[0] = System.currentTimeMillis();
            }
        });

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    try {
                        startBarrier.await();
                    } catch (Exception e) {
                    }

                    try {
                        for (int i = 0; i < loop && failed == null; i++) {
                            if (!lazyLoader.testInstance()) {
                                lazyLoader.getInstance();
                                assertTrue(lazyLoader.testInstance());
                            }
                        }
                    } catch (Throwable e) {
                        failed = e;
                    }
                }
            }, "thread-" + (i + 1));
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        if (failed != null) {
            throw new RuntimeException(failed);
        }

        long totalTime = System.currentTimeMillis() - start[0];

        System.out.printf("%s performance test, concurrency %,d, loop %,d, takes %,d ms\n", loaderType, concurrency,
                loop, totalTime);
        System.out.printf("Creator thread: %s\n", lazyLoader.getInstance().creator);
        System.out.println("------------------------------------");

        return new PerformanceResult(loaderType, totalTime);
    }

    @Test
    public void correctness() throws Exception {
        correctness_test(CONCURRENCY, LOOP, LoaderType.SYNC_LOADER);
        correctness_test(CONCURRENCY, LOOP, LoaderType.PERTHREAD_LOADER);
        correctness_test(CONCURRENCY, LOOP, LoaderType.DCL_LOADER);
    }

    /**
     * 确保:
     * <ul>
     * <li>只初始化一次</li>
     * <li>所有线程都能拿到相同的初始化后的对象</li>
     * <li>执行顺序不会紊乱，即拿到未初始化完成的对象</li>
     * </ul>
     */
    private void correctness_test(final int concurrency, final int loop, final LoaderType loaderType)
            throws InterruptedException {
        long start = System.currentTimeMillis();
        Thread[] threads = new Thread[concurrency];
        final Map<String, Integer> creators = createLinkedHashMap();
        final Resource[] results = new Resource[concurrency];
        final AtomicInteger counter1 = new AtomicInteger();
        final AtomicInteger counter2 = new AtomicInteger();

        final CyclicBarrier startBarrier = new CyclicBarrier(concurrency, new Runnable() {
            public void run() {
                lazyLoader = loaderType.getLoader(); // 清空loader，准备重新装载
                counter1.incrementAndGet();

                assertFalse(lazyLoader.testInstance());
            }
        });
        final CyclicBarrier barrier = new CyclicBarrier(concurrency, new Runnable() {
            public void run() {
                assertTrue(lazyLoader.testInstance());

                Resource r = results[0];

                assertNotNull(r);
                assertNotNull(r.innerObject);

                String creatorThread = lazyLoader.getInstance().creator;

                creators.put(creatorThread, creators.get(creatorThread) + 1);

                for (int i = 1; i < results.length; i++) {
                    Resource ri = results[i];

                    assertSame(r, ri);
                }

                counter2.incrementAndGet();
            }
        });

        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            String threadName = "thread-" + (i + 1);

            creators.put(threadName, 0);

            threads[i] = new Thread(new Runnable() {
                public void run() {
                    try {
                        for (int i = 0; i < loop && failed == null; i++) {
                            startBarrier.await(); // 齐步，走！

                            results[index] = lazyLoader.getInstance();

                            assertNotNull(results[index]);
                            assertNotNull(results[index].innerObject);

                            barrier.await(); // 检查结果
                        }
                    } catch (Throwable e) {
                        failed = e;
                    }
                }
            }, threadName);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        if (failed != null) {
            throw new RuntimeException(failed);
        }

        assertEquals(loop, counter1.intValue());
        assertEquals(loop, counter2.intValue());

        System.out.printf("%s test duration %,d ms\n", loaderType, System.currentTimeMillis() - start);

        // 打印creator分布图
        int i = 0;
        for (int count : creators.values()) {
            System.out.printf("%4d ", count);
            if (++i % 20 == 0) {
                System.out.println();
            }
        }
        System.out.println();

        System.out.println("------------------------------------");
    }

    @Test
    public void exception() throws Exception {
        exception_test(LoaderType.SYNC_LOADER);
        exception_test(LoaderType.PERTHREAD_LOADER);
        exception_test(LoaderType.DCL_LOADER);
    }

    /**
     * 测试exception handler。
     */
    private void exception_test(final LoaderType loaderType) throws InterruptedException {
        LazyLoader<Object, Object> loader;

        // without exception handler
        loader = loaderType.getLoader(new Loader<Object, Object>() {
            public Object load(Object context) {
                throw new RuntimeException("test");
            }
        });

        try {
            loader.getInstance();
            fail();
        } catch (Exception e) {
        }

        assertFalse(loader.testInstance());

        try {
            loader.getInstance();
            fail();
        } catch (Exception e) {
        }

        assertFalse(loader.testInstance());

        // with exception handler
        loader = loaderType.getLoader(new ExceptionHandler<Object, Object>() {
            public Object load(Object context) {
                throw new RuntimeException("test");
            }

            public Object handle(RuntimeException e, Object context) {
                return e;
            }
        });

        Object o = loader.getInstance();

        assertTrue(o instanceof RuntimeException);
        assertEquals("test", ((RuntimeException) o).getMessage());

        assertTrue(loader.testInstance());
        assertSame(o, loader.getInstance());
    }

    @Test
    public void _toString() {
        to_string_test(LoaderType.DCL_LOADER);
        to_string_test(LoaderType.PERTHREAD_LOADER);
        to_string_test(LoaderType.SYNC_LOADER);
    }

    private void to_string_test(LoaderType loaderType) {
        lazyLoader = loaderType.getLoader();
        assertEquals("LazyLoader(LazyLoaderTests.ResourceLoader)", lazyLoader.toString());

        lazyLoader.getInstance();
        assertEquals("LazyLoader(LazyLoaderTests.ResourceLoader, loaded)", lazyLoader.toString());
    }

    /**
     * 测试资源类。
     */
    private static class Resource {
        public Object innerObject;
        public String creator;
    }

    /**
     * Loader类型。
     */
    private static enum LoaderType {
        SYNC_LOADER {
            @Override
            protected <T> LazyLoader<T, Object> getLoader(Loader<T, Object> loader) {
                return LazyLoader.getSynchronizedLazyLoader(loader);
            }
        },
        PERTHREAD_LOADER {
            @Override
            protected <T> LazyLoader<T, Object> getLoader(Loader<T, Object> loader) {
                return LazyLoader.getPerThreadLazyLoader(loader);
            }
        },
        DCL_LOADER {
            @Override
            protected <T> LazyLoader<T, Object> getLoader(Loader<T, Object> loader) {
                return LazyLoader.getDoubleCheckedLockingLazyLoader(loader);
            }
        };

        public final LazyLoader<Resource, Object> getLoader() {
            return getLoader(new ResourceLoader());
        }

        protected abstract <T> LazyLoader<T, Object> getLoader(Loader<T, Object> loader);
    }

    /**
     * 实际的loader。
     */
    private static class ResourceLoader implements Loader<Resource, Object> {
        public Resource load(Object context) {
            Resource r = new Resource();

            r.innerObject = new Object();
            r.creator = Thread.currentThread().getName();

            return r;
        }
    }

    /**
     * 性能数据。
     */
    private static class PerformanceResult implements Comparable<PerformanceResult> {
        public final String name;
        public final long totalTime;

        public PerformanceResult(LoaderType loader, long totalTime) {
            this.name = loader.name();
            this.totalTime = totalTime;
        }

        @Override
        public String toString() {
            return String.format("%s(%,d ms)", name, totalTime);
        }

        public int compareTo(PerformanceResult o) {
            return (int) (totalTime - o.totalTime);
        }
    }
}
