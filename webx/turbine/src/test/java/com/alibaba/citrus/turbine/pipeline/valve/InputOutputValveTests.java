/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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
 */

package com.alibaba.citrus.turbine.pipeline.valve;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static org.junit.Assert.*;

import java.util.Map;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.util.internal.InterfaceImplementorBuilder;
import org.junit.Before;
import org.junit.Test;

public class InputOutputValveTests {
    private AbstractInputOutputValve valve;
    private PipelineContext          context;
    private Map<String, Object> attrs = createHashMap();

    @Before
    public void init() {
        valve = new MyInputOutputValve();
        context = (PipelineContext) new InterfaceImplementorBuilder().addInterface(PipelineContext.class).toObject(new Object() {
            public Object getAttribute(String key) {
                return attrs.get(key);
            }

            public void setAttribute(String key, Object value) {
                attrs.put(key, value);
            }
        });
    }

    @Test
    public void input_systemDefaultValue() {
        assertEquals("result", valve.getInputKey());

        valve = new MyInputOutputValve() {
            @Override
            protected String getDefaultInputKey() {
                return null; // 返回null表示使用系统默认值
            }
        };

        assertEquals("result", valve.getInputKey());

        // 取消默认值
        valve.setInput("specified");
        assertEquals("specified", valve.getInputKey());
    }

    @Test
    public void input_defaultValue() {
        valve = new MyInputOutputValve() {
            @Override
            protected String getDefaultInputKey() {
                return "myin";
            }
        };

        assertEquals("myin", valve.getInputKey());

        // 取消默认值
        valve.setInput("specified");
        assertEquals("specified", valve.getInputKey());
    }

    @Test
    public void input_set() {
        valve.setInput("  ");
        assertEquals("result", valve.getInputKey());

        valve.setInput(" myresult ");
        assertEquals("myresult", valve.getInputKey());
    }

    @Test
    public void out_systemDefaultValue() {
        assertEquals("result", valve.getOutputKey());

        valve = new MyInputOutputValve() {
            @Override
            protected String getDefaultOutputKey() {
                return null; // 返回null表示使用系统默认值
            }
        };

        assertEquals("result", valve.getOutputKey());

        // 取消默认值
        valve.setOutput("specified");
        assertEquals("specified", valve.getOutputKey());
    }

    @Test
    public void out_defaultValue() {
        valve = new MyInputOutputValve() {
            @Override
            protected String getDefaultOutputKey() {
                return "myout";
            }
        };

        assertEquals("myout", valve.getOutputKey());

        // 取消默认值
        valve.setOutput("specified");
        assertEquals("specified", valve.getOutputKey());
    }

    @Test
    public void out_set() {
        valve.setOutput("  ");
        assertEquals("result", valve.getOutputKey());

        valve.setOutput(" myresult ");
        assertEquals("myresult", valve.getOutputKey());
    }

    @Test
    public void getInputValue() {
        assertNull(valve.getInputValue(context));

        attrs.put("result", "hello");
        assertEquals("hello", valve.getInputValue(context));

        assertEquals("hello", valve.getInputValue(context));
    }

    @Test
    public void consumeInputValue() {
        assertNull(valve.getInputValue(context));

        attrs.put("result", "hello");
        assertEquals("hello", valve.consumeInputValue(context));

        assertNull(valve.getInputValue(context));
    }

    @Test
    public void consumeInputValue_withFilter() {
        valve = new MyInputOutputValve() {
            @Override
            protected boolean filterInputValue(Object inputValue) {
                return inputValue instanceof String;
            }
        };

        assertNull(valve.getInputValue(context));

        // filter方法只接受string类型
        attrs.put("result", "hello");
        assertEquals("hello", valve.consumeInputValue(context)); // 对于支持的类型，返回对象
        assertNull(valve.getInputValue(context)); // 并从context中删除该对象

        // 如果是其它类型，则忽略之
        Object obj = new Object();
        attrs.put("result", obj);
        assertSame(obj, valve.getInputValue(context));
        assertSame(null, valve.consumeInputValue(context)); // 对不支持的类型，返回null
        assertSame(obj, valve.getInputValue(context)); // 然后原对象还在context中
    }

    @Test(expected = IllegalArgumentException.class)
    public void consumeInputValue_required() {
        valve = new MyInputOutputValve() {
            @Override
            protected boolean filterInputValue(Object inputValue) {
                // 可以在这个方法里判断值required特性
                // 即：如果值不存在或不符合要求，就抛异常
                if (!(inputValue instanceof String)) {
                    throw new IllegalArgumentException();
                }

                // 如果没有异常，则一定返回true
                return true;
            }
        };

        valve.consumeInputValue(context);
    }

    @Test
    public void differentInputOutputKeys() {
        valve = new MyInputOutputValve() {
            @Override
            protected String getDefaultInputKey() {
                return "myin";
            }

            @Override
            protected String getDefaultOutputKey() {
                return "myout";
            }
        };

        attrs.put("myin", "hello");
        assertEquals("hello", valve.getInputValue(context));

        valve.setOutputValue(context, "hi");
        assertEquals("hello", attrs.get("myin"));
        assertEquals("hi", attrs.get("myout"));
    }

    @Test
    public void setOutputValue() {
        assertNull(valve.getInputValue(context));

        valve.setOutputValue(context, "hi");
        assertEquals("hi", valve.getInputValue(context));
    }

    public static class MyInputOutputValve extends AbstractInputOutputValve {
        public void invoke(PipelineContext pipelineContext) throws Exception {
        }
    }
}
