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

package com.alibaba.citrus.dev.handler.component;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import com.alibaba.citrus.util.internal.webpagelite.PageComponent;
import com.alibaba.citrus.util.internal.webpagelite.PageComponentRegistry;
import com.alibaba.citrus.util.regex.ClassNameWildcardCompiler;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;

/**
 * 用来避免从非授权的机器上访问开发者页面的组件。
 *
 * @author Michael Zhou
 */
public class AccessControlComponent extends PageComponent {
    private static final String PROPERTY_ALLOWD_HOSTS = "developmentMode.allowedHosts";
    private final Pattern[] allowdHostPatterns;

    public AccessControlComponent(PageComponentRegistry registry, String componentPath) {
        super(registry, componentPath);

        String[] allowedHosts = split(defaultIfNull(System.getProperty(PROPERTY_ALLOWD_HOSTS), EMPTY_STRING), ", ");
        List<Pattern> patterns = createLinkedList();

        for (String allowedHost : allowedHosts) {
            patterns.add(ClassNameWildcardCompiler.compileClassName(allowedHost));
        }

        this.allowdHostPatterns = patterns.toArray(new Pattern[patterns.size()]);
    }

    public boolean accessAllowed(RequestHandlerContext context) {
        if (checkPermission(context)) {
            return true;
        } else {
            getTemplate().accept(new AccessDeniedVisitor(context));
            return false;
        }
    }

    private boolean checkPermission(RequestHandlerContext context) {
        String remoteAddr = getRemoteAddr(context);

        try {
            InetAddress addr = InetAddress.getByName(remoteAddr);

            for (Pattern allowedHostPattern : allowdHostPatterns) {
                if (allowedHostPattern.matcher(remoteAddr).matches()) {
                    return true;
                }
            }

            // 总是接受localhost
            if (addr.isLoopbackAddress()) {
                return true;
            } else {
                // 总是接受当前主机中任意一块网卡的任意ip
                for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements(); ) {
                    for (Enumeration<InetAddress> f = e.nextElement().getInetAddresses(); f.hasMoreElements(); ) {
                        if (addr.equals(f.nextElement())) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }

        return false;
    }

    private String getRemoteAddr(RequestHandlerContext context) {
        return trimToNull(context.getRequest().getRemoteAddr());
    }

    @SuppressWarnings("unused")
    private class AccessDeniedVisitor extends AbstractVisitor {
        public AccessDeniedVisitor(RequestHandlerContext context) {
            super(context, AccessControlComponent.this);
        }

        public void visitPropertyName() {
            out().print(PROPERTY_ALLOWD_HOSTS);
        }

        public void visitPropertyValue() {
            String remoteAddr = getRemoteAddr(context);

            if (remoteAddr != null) {
                out().print(remoteAddr);
            }
        }

        public void visitPropertyValueWildcard() {
            String remoteAddr = getRemoteAddr(context);

            if (remoteAddr != null) {
                out().print(remoteAddr.substring(0, remoteAddr.lastIndexOf(".") + 1) + "*");
            }
        }
    }
}
