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
package com.alibaba.citrus.service.form.support;

/**
 * 代表比较操作符。
 * 
 * @author Michael Zhou
 */
public enum CompareOperator {
    equalTo {
        @Override
        public boolean accept(int compareResult) {
            return compareResult == 0;
        }
    },
    notEqualTo {
        @Override
        public boolean accept(int compareResult) {
            return compareResult != 0;
        }
    },
    lessThan {
        @Override
        public boolean accept(int compareResult) {
            return compareResult < 0;
        }
    },
    greaterThan {
        @Override
        public boolean accept(int compareResult) {
            return compareResult > 0;
        }
    },
    lessThanOrEqualTo {
        @Override
        public boolean accept(int compareResult) {
            return compareResult <= 0;
        }
    },
    greaterThanOrEqualTo {
        @Override
        public boolean accept(int compareResult) {
            return compareResult >= 0;
        }
    };

    public abstract boolean accept(int compareResult);
}
