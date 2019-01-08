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

package com.alibaba.citrus.springext.support.parser;

import static com.alibaba.citrus.generictype.TypeInfoUtil.*;

import org.w3c.dom.Element;

import com.alibaba.citrus.util.StringUtil;

/**
 * 定义一个bean definition，根据generic type取得bean class。
 *
 * @author Michael Zhou
 */
public abstract class AbstractSingleBeanDefinitionParser<T> extends
                                                            org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser {
    @Override
    protected final Class<?> getBeanClass(Element element) {
        return resolveParameter(getClass(), AbstractSingleBeanDefinitionParser.class, 0).getRawType();
    }

	/*@Override
	protected String getBeanClassName(Element element) {
		String name=element.getAttribute(this.ID_ATTRIBUTE);
		if(StringUtil.isEmpty(name)) {
			name=element.getAttribute(this.NAME_ATTRIBUTE);
		}
		return name;
	}*/
    
    
}
