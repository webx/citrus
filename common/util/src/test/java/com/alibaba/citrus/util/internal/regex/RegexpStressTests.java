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
package com.alibaba.citrus.util.internal.regex;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.util.List;

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RegexpStressTests {
    private String[] exps;
    private String[] data;
    private int matchedCount;

    public RegexpStressTests(String[] exps, String[] data, int matchedCount) {
        this.exps = exps;
        this.data = data;
        this.matchedCount = matchedCount;
    }

    @Parameters
    public static List<Object[]> data() {
        List<Object[]> list = createLinkedList();

        String[] data = new String[] { "control.setTemplate(aaa)", "screen_placeholder",
                "stringEscapeUtil.escape($abc)", "csrfToken.getHiddenField()",
                "sdfasdfasldkfjqwpoieruqpweiorjalkfjasd;klfjasdpoifuqwpioerjkjdsfsladfjpqoieadsfkasdfdaksfj;asdkfa;sdwr" };

        add(list, new String[] { "^control\\.", "^screen_placeholder", "^stringEscapeUtil\\.escape",
                "^csrfToken\\.(get)?hiddenField", "dsfsladf\\w+qo.*ds" }, data, 5);

        add(list, new String[] { "(^control\\.)" + //
                "|(^screen_placeholder)" + //
                "|(^stringEscapeUtil\\.escape)" + //
                "|(^csrfToken\\.(get)?hiddenField)" + //
                "|(dsfsladf\\w+qo.*ds)" }, data, 5);

        return list;
    }

    private static void add(List<Object[]> list, String[] exps, String[] data, int matchedCount) {
        list.add(new Object[] { exps, data, matchedCount });
    }

    @Test
    public void oro() throws Exception {
        test(new OroTester(), 10000, 10);
    }

    @Test
    public void jdk() throws Exception {
        test(new JdkRegexTester(), 10000, 10);
    }

    private void test(final Tester test, final int loop, final int concurrency) throws Exception {
        test.warmUp();

        Thread[] ts1 = new Thread[concurrency];

        long start = System.currentTimeMillis();

        for (int i = 0; i < ts1.length; i++) {
            ts1[i] = new Thread(new Runnable() {
                public void run() {
                    test.run(loop);
                }
            });
        }

        for (Thread element : ts1) {
            element.start();
        }

        for (Thread element : ts1) {
            element.join();
        }

        long duration = System.currentTimeMillis() - start;
        int matches = loop * exps.length * data.length * concurrency;

        System.out.printf("%s - %,d ms for %,d matches%n", test, duration, matches);
        System.out.printf("%s \u03BCs%n", (double) duration / (matches / 1000));
    }

    interface Tester {
        void run(int loop);

        void warmUp();
    }

    class OroTester implements Tester {
        private Pattern[] patterns;

        public OroTester() throws Exception {
            patterns = new Pattern[exps.length];

            for (int i = 0; i < patterns.length; i++) {
                patterns[i] = new Perl5Compiler().compile(exps[i], Perl5Compiler.CASE_INSENSITIVE_MASK
                        | Perl5Compiler.READ_ONLY_MASK);
            }
        }

        public void run(int loop) {
            PatternMatcher matcher = new Perl5Matcher();

            for (int i = 0; i < loop; i++) {
                for (Pattern pattern : patterns) {
                    for (String value : data) {
                        matcher.contains(value, pattern);
                    }
                }
            }
        }

        public void warmUp() {
            PatternMatcher matcher = new Perl5Matcher();
            int matchedCount = 0;

            for (Pattern pattern : patterns) {
                for (String value : data) {
                    if (matcher.contains(value, pattern)) {
                        matchedCount++;
                    }
                }
            }

            assertEquals(RegexpStressTests.this.matchedCount, matchedCount);
        }

        @Override
        public String toString() {
            return "ORO regex";
        }
    }

    class JdkRegexTester implements Tester {
        private java.util.regex.Pattern[] patterns;

        public JdkRegexTester() throws Exception {
            patterns = new java.util.regex.Pattern[exps.length];

            for (int i = 0; i < patterns.length; i++) {
                patterns[i] = java.util.regex.Pattern.compile(exps[i], java.util.regex.Pattern.CASE_INSENSITIVE);
            }
        }

        public void run(int loop) {
            for (int i = 0; i < loop; i++) {
                for (java.util.regex.Pattern pattern : patterns) {
                    for (String value : data) {
                        pattern.matcher(value).find();
                    }
                }
            }
        }

        public void warmUp() {
            int matchedCount = 0;

            for (java.util.regex.Pattern pattern : patterns) {
                for (String value : data) {
                    if (pattern.matcher(value).find()) {
                        matchedCount++;
                    }
                }
            }

            assertEquals(RegexpStressTests.this.matchedCount, matchedCount);
        }

        @Override
        public String toString() {
            return "JDK regex";
        }
    }
}
