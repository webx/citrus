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
package com.alibaba.citrus.service.form.impl.validation.composite;

import static com.alibaba.citrus.springext.util.DomUtil.*;
import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.StringUtil.*;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.form.Condition;
import com.alibaba.citrus.service.form.impl.condition.JexlCondition;
import com.alibaba.citrus.service.form.support.AbstractCompositeValidatorDefinitionParser;
import com.alibaba.citrus.service.form.support.AbstractSimpleCompositeValidator;
import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;

/**
 * 用于条件判断的validator。
 * 
 * @author Michael Zhou
 */
public class IfValidator extends AbstractSimpleCompositeValidator {
    private Condition condition;

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    protected boolean requiresMessage() {
        return false;
    }

    @Override
    protected void init() throws Exception {
        super.init();
        assertNotNull(condition, "no condition");
    }

    public boolean validate(Context context) {
        return !condition.isSatisfied(context) || doValidate(context);
    }

    public static class DefinitionParser extends AbstractCompositeValidatorDefinitionParser<IfValidator> implements
            ContributionAware {
        private ConfigurationPoint conditionConfigurationPoint;

        @Override
        public void setContribution(Contribution contrib) {
            super.setContribution(contrib);
            this.conditionConfigurationPoint = getSiblingConfigurationPoint("services/form/conditions", contrib);
        }

        @Override
        protected void doParseAttributes(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            String testExpr = trimToNull(element.getAttribute("test"));

            if (testExpr != null) {
                BeanDefinitionBuilder jexlCondition = BeanDefinitionBuilder.genericBeanDefinition(JexlCondition.class);
                jexlCondition.addConstructorArgValue(testExpr);
                builder.addPropertyValue("condition", jexlCondition.getBeanDefinition());
            }
        }

        @Override
        protected void doParseElement(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            super.doParseElement(element, parserContext, builder);

            for (Element subElement : subElements(element)) {
                Object condition = parseConfigurationPointBean(subElement, conditionConfigurationPoint, parserContext,
                        builder);

                if (condition != null) {
                    builder.addPropertyValue("condition", condition);
                    break;
                }
            }
        }
    }
}
