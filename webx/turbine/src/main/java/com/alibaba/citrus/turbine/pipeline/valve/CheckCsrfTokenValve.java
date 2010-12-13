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
package com.alibaba.citrus.turbine.pipeline.valve;

import static com.alibaba.citrus.springext.util.SpringExtUtil.*;
import static com.alibaba.citrus.turbine.util.TurbineUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.logconfig.support.SecurityLogger;
import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.citrus.service.pipeline.support.AbstractValveDefinitionParser;
import com.alibaba.citrus.turbine.TurbineRunData;
import com.alibaba.citrus.turbine.util.CsrfToken;
import com.alibaba.citrus.turbine.util.CsrfTokenCheckException;
import com.alibaba.citrus.util.StringUtil;

/**
 * 用来检查<code>CsrfToken</code>的valve，用来防止csrf攻击和重复提交同一表单。
 * 
 * @author Michael Zhou
 */
public class CheckCsrfTokenValve extends AbstractValve {
    private final SecurityLogger log = new SecurityLogger();

    @Autowired
    private HttpServletRequest request;

    private String tokenKey;
    private int maxTokens;
    private String expiredPage;

    public String getTokenKey() {
        return tokenKey;
    }

    public void setTokenKey(String tokenKey) {
        this.tokenKey = trimToNull(tokenKey);
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public String getExpiredPage() {
        return expiredPage;
    }

    public void setExpiredPage(String expiredPage) {
        this.expiredPage = expiredPage;
    }

    public String getLogName() {
        return this.log.getLogger().getName();
    }

    public void setLogName(String logName) {
        this.log.setLogName(logName);
    }

    @Override
    protected void init() {
        tokenKey = defaultIfNull(tokenKey, CsrfToken.DEFAULT_TOKEN_KEY);
    }

    /**
     * 如果csrf不符，则重定向到出错页面。
     */
    public void invoke(PipelineContext pipelineContext) throws Exception {
        TurbineRunData rundata = getTurbineRunData(request);

        // 获取request中的csrf值
        String tokenFromRequest = StringUtil.trimToNull(rundata.getParameters().getString(tokenKey));

        if (tokenFromRequest != null) {
            HttpSession session = rundata.getRequest().getSession();

            // 先检查longLiveToken，如果匹配，则不用检查uniqueToken了。 
            if (!tokenFromRequest.equals(CsrfToken.getLongLiveTokenInSession(session))) {
                List<String> tokensInSession = CsrfToken.getTokensInSession(session, tokenKey);

                if (!tokensInSession.contains(tokenFromRequest)) {
                    // 如果不符则终止请求
                    requestExpired(rundata, tokenFromRequest, tokensInSession);
                } else {
                    // 如果符合，则清除session中相应的token，以防止再次使用它
                    tokensInSession.remove(tokenFromRequest);

                    CsrfToken.setTokensInSession(session, tokenKey, tokensInSession);
                }
            }
        }

        try {
            // 在thread上下文中保存当前的tokenKey，以便使其它csrfToken的检查都能使用统一的key。
            CsrfToken.setContextTokenConfiguration(tokenKey, maxTokens);
            pipelineContext.invokeNext();
        } finally {
            CsrfToken.resetContextTokenConfiguration();
        }
    }

    private void requestExpired(TurbineRunData rundata, String tokenFromRequest, List<String> tokensInSession) {
        log.getLogger().warn("CsrfToken \"{}\" does not match: requested token is {}, but the session tokens are {}.",
                new Object[] { tokenKey, tokenFromRequest, tokensInSession });

        // 有两种处理方法，1. 显示expiredPage；2. 抛出异常。
        if (expiredPage != null) {
            rundata.setRedirectTarget(expiredPage);
        } else if (expiredPage == null) {
            throw new CsrfTokenCheckException(rundata.getRequest().getRequestURL().toString());
        }
    }

    public static class DefinitionParser extends AbstractValveDefinitionParser<CheckCsrfTokenValve> {
        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            attributesToProperties(element, builder, "tokenKey", "maxTokens", "expiredPage", "logName");
        }
    }
}
