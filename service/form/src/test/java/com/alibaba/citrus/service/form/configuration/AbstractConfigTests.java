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

package com.alibaba.citrus.service.form.configuration;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.List;

import com.alibaba.citrus.service.form.Validator;
import com.alibaba.citrus.service.form.configuration.GroupConfig.Import;
import com.alibaba.citrus.service.form.impl.configuration.FieldConfigImpl;
import com.alibaba.citrus.service.form.impl.configuration.GroupConfigImpl;

public abstract class AbstractConfigTests {
    protected final List<FieldConfigImpl> createFieldList(FieldConfigImpl... fields) {
        return createArrayList(fields);
    }

    protected final List<GroupConfigImpl> createGroupList(GroupConfigImpl... groups) {
        return createArrayList(groups);
    }

    protected final List<Import> createImportList(Import... imports) {
        return createArrayList(imports);
    }

    protected final List<Validator> createValidatorList(Validator... validators) {
        return createArrayList(validators);
    }
}
