/*
 * Copyright 2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.citrus.service.mappingrule.impl.rule;

import static com.alibaba.citrus.springext.util.SpringExtUtil.attributesToProperties;
import static com.alibaba.citrus.util.StringUtil.substring;
import static com.alibaba.citrus.service.moduleloader.constant.ModuleConstant.DEFAULT_EXECUTE_METHOD;
import static com.alibaba.citrus.service.moduleloader.constant.ModuleConstant.EVENT_HANDLER_METHOD;
import static org.springframework.util.StringUtils.uncapitalize;
import static org.springframework.util.StringUtils.capitalize;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.service.mappingrule.MappingRuleException;
import com.alibaba.citrus.service.mappingrule.support.AbstractModuleMappingRuleDefinitionParser;
import com.alibaba.citrus.service.moduleloader.Module;
import com.alibaba.citrus.service.moduleloader.ModuleLoaderException;
import com.alibaba.citrus.service.moduleloader.impl.adapter.HandlerModule;

/**
 * 在兼容<code>FallbackModuleMappingRule<code>的基础上，增加类似RestFul风格实现：<p>
 *  兼容策略有两种:
 *  <ol>
 *     <li>（默认）：先根据<code>FallbackModuleMappingRule<code>的策略查找对应MapName，然后才根据RestFul风格去查找对应MapName</li>
 *        <p>
 *           例如：将模板名：<code>"about/directions/driving.vm"</code>映射到screen module，将顺次搜索以下module： </p>
 * <ol>
 * <li><code>&quot;about.directions.Driving&quot;</code></li>
 * <li><code>&quot;about.directions.Default&quot;</code></li>
 * <li><code>&quot;about.Default&quot;</code></li>
 * <li><code>&quot;Default&quot;</code></li>
 * <li><code>&quot;DefaultScreen&quot;</code>（即配置文件中指定的默认module名）</li>
 * <li><strong><code>&quot;about.Directions&quot;</code></strong>意味着Directions类里含有driving方法</li>
 * </ol>
 * <p>
 * <li>混杂查找策略</li>
 * <p>
 * 例如：将模板名：<code>"about/directions/driving.vm"</code>映射到screen module，将顺次搜索以下module：
 * </p>
 * <ol>
 * <li><code>&quot;about.directions.Driving&quot;</code></li>
 * <li><strong><code>&quot;about.Directions&quot;</code></strong>意味着Directions类里含有driving方法</li>
 * <li><code>&quot;about.directions.Default&quot;</code></li>
 * <li><code>&quot;about.Default&quot;</code></li>
 * <li><code>&quot;Default&quot;</code></li>
 * <li><code>&quot;DefaultScreen&quot;</code>（即配置文件中指定的默认module名）</li>
 * </ol>
 * <p>
 * </ol> 注意，无论上述哪种策略，如果上例中<code>DefaultScreen</code>不存在或未指定默认值，则返回最初的结果： <code>about.directions.Driving</code>。
 * 
 * @author qianchao 2012-6-29 上午9:09:02
 */
public class RestFullModuleMappingRule extends FallbackModuleMappingRule {

    private boolean            restFulPrior = false;
    @Autowired
    private HttpServletRequest request;

    public void setRestFulPrior(boolean restFulPrior) {
		this.restFulPrior = restFulPrior;
	}

	@Override
    public String doMapping(String name) {
        FallbackIterator iter = new FallbackModuleIterator(name, getDefaultName(), isMatchLastName());

        String moduleName = null;
        String firstModuleName = iter.getNext(); // 保存第一个精确的匹配，万一找不到，就返回这个值

        if (!restFulPrior) {
            // 策略1
            moduleName = getFallBackMapName(iter);
            if (moduleName != null) {
                return moduleName;
            }

            moduleName = getRestFullMapName(firstModuleName);
            if (moduleName != null) {
                return moduleName;
            }

        } else {
            // 策略2
            moduleName = iter.next();
            try {
                if (checkRequestModuleExist(getModuleLoaderService().getModuleQuiet(getModuleType(), moduleName),DEFAULT_EXECUTE_METHOD)) {
                    return moduleName;
                } // else 继续查找
            } catch (ModuleLoaderException e) {
                throw new MappingRuleException(e);
            }
            moduleName = getRestFullMapName(moduleName);
            if (moduleName != null) {
                return moduleName;
            }
            moduleName = getFallBackMapName(iter);
            if (moduleName != null) {
                return moduleName;
            }
        }

        return firstModuleName;
    }

    private String getRestFullMapName(String fullModuleName) {
        if (fullModuleName != null) {
            int lastSeparatorPos = fullModuleName.lastIndexOf(EXTENSION_SEPARATOR);
            if (lastSeparatorPos > 0) {
                String methodName = uncapitalize(substring(fullModuleName, lastSeparatorPos+1));
                String moduleName = capitalizeModuleName(substring(fullModuleName, 0, lastSeparatorPos));
                if (checkRequestModuleExist(getModuleLoaderService().getModuleQuiet(getModuleType(), moduleName),methodName)) {
                    return moduleName;
                }
            }
        }
        return null;
    }
    
    private String capitalizeModuleName(String rawModuleName){
    	int lastSeparatorPos = rawModuleName.lastIndexOf(EXTENSION_SEPARATOR);
    	if(lastSeparatorPos>0){
    		String prefix = substring(rawModuleName,0,lastSeparatorPos);
    		String last = capitalize(substring(rawModuleName,lastSeparatorPos+1));
    		return prefix+EXTENSION_SEPARATOR+last;
    	}
    	return rawModuleName;
    }

    private boolean checkRequestModuleExist(Module module, String methodName) {
        if (module instanceof HandlerModule) {
            HandlerModule handlerModule = (HandlerModule) module;
            if (handlerModule.getHandlers().containsKey(methodName)) {
                request.setAttribute(EVENT_HANDLER_METHOD, methodName);
                return true;
            }
        }
        return false;
    }

    /**
     * 递归判断Module是否含有execute方法
     */
    private String getFallBackMapName(FallbackIterator iter) {
        String moduleName = null;
        while (iter.hasNext()) {
            moduleName = iter.next();

            log.debug("Looking for module: " + moduleName);

            try {
            	if(checkRequestModuleExist(getModuleLoaderService().getModuleQuiet(getModuleType(), moduleName),DEFAULT_EXECUTE_METHOD)){
                    return moduleName;
                } // else 继续查找
            } catch (ModuleLoaderException e) {
                throw new MappingRuleException(e);
            }
        }
        return null;
    }

    public static class DefinitionParser extends AbstractModuleMappingRuleDefinitionParser<RestFullModuleMappingRule> {

        @Override
        protected void doParseModuleMappingRule(Element element, ParserContext parserContext,
                                                BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "defaultName", "matchLastName", "restFulPrior");
        }
    }

}
