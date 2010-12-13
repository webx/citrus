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
package com.alibaba.citrus.springext.impl;

import static com.alibaba.citrus.util.Assert.*;

import com.alibaba.citrus.springext.ContributionType;

/**
 * 用来索引contributions的key。
 * 
 * @author Michael Zhou
 */
class ContributionKey implements Comparable<ContributionKey> {
    private final String name;
    private final ContributionType type;

    public ContributionKey(String name, ContributionType type) {
        this.name = assertNotNull(name, "name");
        this.type = assertNotNull(type, "type");
    }

    public String getName() {
        return name;
    }

    public ContributionType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof ContributionKey)) {
            return false;
        }

        ContributionKey other = (ContributionKey) obj;

        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }

        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }

        return true;
    }

    public int compareTo(ContributionKey o2) {
        int result = name.compareTo(o2.name);

        if (result == 0) {
            result = type.compareTo(o2.type);
        }

        return result;
    }

    @Override
    public String toString() {
        return "ContributionKey[" + name + ", " + type + "]";
    }
}
