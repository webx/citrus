package com.alibaba.citrus.service.form.configuration;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.List;

import com.alibaba.citrus.service.form.Validator;
import com.alibaba.citrus.service.form.configuration.GroupConfig.Import;
import com.alibaba.citrus.service.form.impl.configuration.FieldConfigImpl;
import com.alibaba.citrus.service.form.impl.configuration.GroupConfigImpl;
import com.alibaba.citrus.service.form.impl.configuration.GroupConfigImpl.ImportImpl;

public abstract class AbstractConfigTests {
    protected final List<FieldConfigImpl> createFieldList(FieldConfigImpl... fields) {
        return createArrayList(fields);
    }

    protected final List<GroupConfigImpl> createGroupList(GroupConfigImpl... groups) {
        return createArrayList(groups);
    }

    protected final List<Import> createImportList(ImportImpl... imports) {
        return createArrayList(imports);
    }

    protected final List<Validator> createValidatorList(Validator... validators) {
        return createArrayList(validators);
    }
}
