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

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.form.Condition;
import com.alibaba.citrus.service.form.Validator;
import com.alibaba.citrus.service.form.impl.condition.JexlCondition;
import com.alibaba.citrus.service.form.support.AbstractCompositeValidator;
import com.alibaba.citrus.service.form.support.AbstractCompositeValidatorDefinitionParser;
import com.alibaba.citrus.service.form.support.AbstractSimpleCompositeValidator;
import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionAware;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;

/**
 * 支持多条件分支的validator。
 * 
 * @author Michael Zhou
 */
public class ChooseValidator extends AbstractCompositeValidator {
    @Override
    protected boolean requiresMessage() {
        return false;
    }

    @Override
    protected void init() throws Exception {
        List<Validator> validators = getValidators();

        assertTrue(!validators.isEmpty(), "no validators");

        for (Iterator<Validator> i = validators.iterator(); i.hasNext();) {
            Validator validator = i.next();

            if (i.hasNext()) {
                assertTrue(validator instanceof When, "expected <when>");
            } else {
                assertTrue(validator instanceof When || validator instanceof Otherwise,
                        "expected <when> or <otherwise>");
            }
        }
    }

    public boolean validate(Context context) {
        for (Validator validator : getValidators()) {
            if (validator instanceof When) {
                When when = (When) validator;
                Condition cond = when.condition;

                if (cond.isSatisfied(context)) {
                    return when.validateInChoose(context);
                }
            } else if (validator instanceof Otherwise) {
                Otherwise ow = (Otherwise) validator;
                return ow.validateInChoose(context);
            }
        }

        return true;
    }

    public static class When extends AbstractSimpleCompositeValidator {
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
            throw new UnsupportedOperationException();
        }

        boolean validateInChoose(Context context) {
            return doValidate(context);
        }
    }

    public static class Otherwise extends AbstractSimpleCompositeValidator {
        @Override
        protected boolean requiresMessage() {
            return false;
        }

        public boolean validate(Context context) {
            throw new UnsupportedOperationException();
        }

        boolean validateInChoose(Context context) {
            return doValidate(context);
        }
    }

    public static class DefinitionParser extends AbstractCompositeValidatorDefinitionParser<ChooseValidator> implements
            ContributionAware {
        private ConfigurationPoint conditionConfigurationPoint;

        @Override
        public void setContribution(Contribution contrib) {
            super.setContribution(contrib);
            this.conditionConfigurationPoint = getSiblingConfigurationPoint("services/form/conditions", contrib);
        }

        @Override
        protected void doParseElement(Element element, ParserContext parserContext, BeanDefinitionBuilder chooseBuilder) {
            ElementSelector whenSelector = and(sameNs(element), name("when"));
            ElementSelector otherwiseSelector = and(sameNs(element), name("otherwise"));
            List<Object> validators = createManagedList(element, parserContext);

            for (Element subElement : subElements(element)) {
                if (whenSelector.accept(subElement)) {
                    validators.add(parseWhen(subElement, parserContext));
                } else if (otherwiseSelector.accept(subElement)) {
                    validators.add(parseOtherwise(subElement, parserContext));
                }
            }

            chooseBuilder.addPropertyValue("validators", validators);
        }

        private Object parseWhen(Element element, ParserContext parserContext) {
            BeanDefinitionBuilder whenBuilder = BeanDefinitionBuilder.genericBeanDefinition(When.class);
            super.doParseElement(element, parserContext, whenBuilder);

            String testExpr = trimToNull(element.getAttribute("test"));

            if (testExpr != null) {
                BeanDefinitionBuilder jexlCondition = BeanDefinitionBuilder.genericBeanDefinition(JexlCondition.class);
                jexlCondition.addConstructorArgValue(testExpr);
                whenBuilder.addPropertyValue("condition", jexlCondition.getBeanDefinition());
            }

            for (Element subElement : subElements(element)) {
                Object condition = parseConfigurationPointBean(subElement, conditionConfigurationPoint, parserContext,
                        whenBuilder);

                if (condition != null) {
                    whenBuilder.addPropertyValue("condition", condition);
                    break;
                }
            }

            return whenBuilder.getBeanDefinition();
        }

        private Object parseOtherwise(Element element, ParserContext parserContext) {
            BeanDefinitionBuilder otherwiseBuilder = BeanDefinitionBuilder.genericBeanDefinition(Otherwise.class);
            super.doParseElement(element, parserContext, otherwiseBuilder);
            return otherwiseBuilder.getBeanDefinition();
        }
    }
}
