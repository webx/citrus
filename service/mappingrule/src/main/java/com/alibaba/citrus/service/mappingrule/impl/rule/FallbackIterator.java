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
package com.alibaba.citrus.service.mappingrule.impl.rule;

import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.alibaba.citrus.service.mappingrule.support.AbstractMappingRule;
import com.alibaba.citrus.util.ArrayUtil;
import com.alibaba.citrus.util.CollectionUtil;
import com.alibaba.citrus.util.StringUtil;

/**
 * 用来遍历可选的module或template的名称。
 * 
 * @author Michael Zhou
 */
abstract class FallbackIterator implements Iterator<String> {
    private static final int STATE_SPECIFIC_NAME = 0;
    private static final int STATE_DEFAULT_NAME = 1;
    private static final int STATE_FINAL_NAME = 2;
    private final String name;
    private final List<String> names;
    private final String defaultLastName;
    private final String finalName;
    private final boolean matchLastName;
    private String lastName;
    private String fullName;
    private int state;

    public FallbackIterator(String name, String defaultLastName, String finalName, boolean matchLastName) {
        String[] parts = StringUtil.split(trimToNull(name), AbstractMappingRule.NAME_SEPARATOR);

        if (ArrayUtil.isEmptyArray(parts)) {
            invalidName(name);
        }

        this.name = name;
        this.defaultLastName = defaultLastName;

        this.names = CollectionUtil.createArrayList(parts);
        this.finalName = finalName;
        this.matchLastName = matchLastName;

        this.state = STATE_SPECIFIC_NAME;
    }

    protected void init() {
        if (lastName == null) {
            lastName = normalizeLastName(names.get(names.size() - 1));

            if (lastName == null) {
                invalidName(name);
            }

            setFullName(lastName);
        }
    }

    public final String getLastName() {
        init();
        return lastName;
    }

    public final String getNext() {
        if (hasNext()) {
            return fullName;
        }

        throw new NoSuchElementException();
    }

    public final boolean hasNext() {
        init();

        if (fullName != null) {
            return true;
        }

        switch (state) {
            case STATE_DEFAULT_NAME:
                if (names.size() <= 1) {
                    state = STATE_FINAL_NAME;

                    if (finalName != null) {
                        fullName = finalName;
                        return true;
                    } else {
                        return false;
                    }
                }

                // 缩减一格
                names.remove(names.size() - 1);

                if (matchLastName) {
                    setFullName(lastName);
                    state = STATE_SPECIFIC_NAME;
                } else {
                    setFullName(defaultLastName);
                    state = STATE_DEFAULT_NAME;
                }

                return true;

            case STATE_SPECIFIC_NAME:
                setFullName(defaultLastName);
                state = STATE_DEFAULT_NAME;
                return true;

            case STATE_FINAL_NAME:
                return false;

            default:
                throw new IllegalStateException();
        }
    }

    private void setFullName(String lastName) {
        names.set(names.size() - 1, lastName);
        fullName = generateFullName(names);
    }

    public final String next() {
        if (hasNext()) {
            String result = fullName;

            fullName = null;

            return result;
        }

        throw new NoSuchElementException();
    }

    public final void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return the names
     */
    public List<String> getNames() {
        return names;
    }

    /**
     * 非法名称，抛出异常。
     */
    protected abstract void invalidName(String name);

    /**
     * 处理最后一个名字。
     */
    protected abstract String normalizeLastName(String lastName);

    /**
     * 生成名字。
     */
    protected abstract String generateFullName(List<String> names);
}
