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
package com.alibaba.citrus.webx.handler.component;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import com.alibaba.citrus.service.configuration.ProductionModeAware;
import com.alibaba.citrus.util.ArrayUtil;
import com.alibaba.citrus.util.HumanReadableSize;
import com.alibaba.citrus.util.SystemUtil;
import com.alibaba.citrus.util.internal.webpagelite.PageComponent;
import com.alibaba.citrus.util.internal.webpagelite.PageComponentRegistry;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;
import com.alibaba.citrus.webx.handler.support.AbstractVisitor;
import com.alibaba.citrus.webx.util.WebxUtil;

/**
 * 显示基本的系统信息。
 * 
 * @author Michael Zhou
 */
public class SystemInfoComponent extends PageComponent implements ProductionModeAware {
    private final KeyValuesComponent keyValuesComponent;
    private boolean productionMode;

    public SystemInfoComponent(PageComponentRegistry registry, String componentPath,
                               KeyValuesComponent keyValuesComponent) {
        super(registry, componentPath);
        this.keyValuesComponent = keyValuesComponent;
    }

    public void setProductionMode(boolean productionMode) {
        this.productionMode = productionMode;
    }

    public void visitTemplate(RequestHandlerContext context) {
        getTemplate().accept(new SystemInfoVisitor(context));
    }

    @SuppressWarnings("unused")
    private class SystemInfoVisitor extends AbstractVisitor {
        public SystemInfoVisitor(RequestHandlerContext context) {
            super(context, SystemInfoComponent.this);
        }

        public void visitSysinfo() {
            Map<String, Object> keyValues = createLinkedHashMap();

            // Webx info
            keyValues.put("Webx Version", WebxUtil.getWebxVersion());
            keyValues.put("Running Mode", productionMode ? "Production Mode" : "Development Mode");

            // Java info
            keyValues.put("Java", ArrayUtil.arrayToMap(new Object[][] { //
                    { "Vendor", SystemUtil.getJavaInfo().getVendor() }, // 
                            { "Version", SystemUtil.getJavaInfo().getVersion() }, // 
                    }, String.class, String.class));

            keyValues.put("Java Runtime", ArrayUtil.arrayToMap(new Object[][] { //
                    { "Name", SystemUtil.getJavaRuntimeInfo().getName() }, // 
                            { "Version", SystemUtil.getJavaRuntimeInfo().getVersion() }, // 
                            { "Home", SystemUtil.getJavaRuntimeInfo().getHomeDir() }, // 
                    }, String.class, String.class));

            keyValues.put("Java Spec.", ArrayUtil.arrayToMap(new Object[][] { //
                    { "Name", SystemUtil.getJavaSpecInfo().getName() }, // 
                            { "Vendor", SystemUtil.getJavaSpecInfo().getVendor() }, // 
                            { "Version", SystemUtil.getJavaSpecInfo().getVersion() }, // 
                    }, String.class, String.class));

            keyValues.put("JVM", ArrayUtil.arrayToMap(new Object[][] { //
                    { "Name", SystemUtil.getJvmInfo().getName() }, // 
                            { "Vendor", SystemUtil.getJvmInfo().getVendor() }, // 
                            { "Version", SystemUtil.getJvmInfo().getVersion() }, // 
                            { "Info", SystemUtil.getJvmInfo().getInfo() }, // 
                    }, String.class, String.class));

            keyValues.put("JVM Spec.", ArrayUtil.arrayToMap(new Object[][] { //
                    { "Name", SystemUtil.getJvmSpecInfo().getName() }, // 
                            { "Vendor", SystemUtil.getJvmSpecInfo().getVendor() }, // 
                            { "Version", SystemUtil.getJvmSpecInfo().getVersion() }, // 
                    }, String.class, String.class));

            // OS info
            keyValues.put("OS", String.format("%s %s %s", SystemUtil.getOsInfo().getName(), SystemUtil.getOsInfo()
                    .getArch(), SystemUtil.getOsInfo().getVersion()));

            keyValues.put("File Encoding", SystemUtil.getOsInfo().getFileEncoding());
            keyValues.put("Host Name", SystemUtil.getHostInfo().getName());
            keyValues.put("Host Address", SystemUtil.getHostInfo().getAddress());

            // IPs
            try {
                Map<String, List<String>> networkAddresses = createTreeMap();

                for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {
                    NetworkInterface networkInterface = e.nextElement();

                    String name = networkInterface.getDisplayName();
                    List<String> addresses = createLinkedList();

                    for (Enumeration<InetAddress> e2 = networkInterface.getInetAddresses(); e2.hasMoreElements();) {
                        addresses.add(e2.nextElement().getHostAddress());
                    }

                    networkAddresses.put(name, addresses);
                }

                keyValues.put("Server IPs", networkAddresses);
            } catch (Exception e) {
                keyValues.put("Server IPs", e.getMessage());
            }

            // Memory
            Map<String, String> memory = createLinkedHashMap();

            memory.put("Free", formatMemory(Runtime.getRuntime().freeMemory()));
            memory.put("Total", formatMemory(Runtime.getRuntime().totalMemory()));
            memory.put("Maximum", formatMemory(Runtime.getRuntime().maxMemory()));

            keyValues.put("Memory", memory);

            keyValuesComponent.visitTemplate(context, keyValues);
        }

        private String formatMemory(long memory) {
            return String.format("%s (%,d)", new HumanReadableSize(memory), memory);
        }
    }
}
