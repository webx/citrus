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
package com.alibaba.citrus.logconfig;

import static com.alibaba.citrus.logconfig.LogConfigurator.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 从init-param中取得logSystem和logConfiguration，并初始化。
 * 
 * @author Michael Zhou
 */
public class LogConfiguratorListener implements ServletContextListener {
    private static final String LOG_CONFIGURATION = "logConfiguration";
    private static final String LOG_SYSTEM = "logSystem";
    private static final String LOG_PREFIX = "log";

    private LogConfigurator[] logConfigurators;

    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();

        // 取得所有以log开头的init params。
        Map<String, String> params = getLogInitParams(servletContext);

        // 从context init-param中取得logSystem的值，可能为null。
        String[] logSystems = getLogSystems(params);

        // 取得指定名称的logConfigurator，如未取得，则抛出异常，listener失败。
        logConfigurators = LogConfigurator.getConfigurators(logSystems);

        for (LogConfigurator logConfigurator : logConfigurators) {
            String logSystem = logConfigurator.getLogSystem();

            // 取得指定logConfigurator的配置文件。
            String logConfiguration = getLogConfiguration(params, logSystem);

            servletContext.log(String.format("Initializing %s system", logSystem));

            // 取得log配置文件。
            URL logConfigurationResource;

            try {
                logConfigurationResource = servletContext.getResource(logConfiguration);
            } catch (MalformedURLException e) {
                logConfigurationResource = null;
            }

            // 如未找到配置文件，则用默认的值来配置，否则配置之。
            if (logConfigurationResource == null) {
                servletContext
                        .log(String
                                .format("Could not find %s configuration file \"%s\" in webapp context.  Using default configurations.",
                                        logSystem, logConfiguration));

                logConfigurator.configureDefault();
            } else {
                Map<String, String> props = logConfigurator.getDefaultProperties();
                initProperties(props);
                props.putAll(params);

                logConfigurator.configure(logConfigurationResource, props);
            }
        }
    }

    /**
     * 子类可覆盖，并创建自己的placeholders。
     */
    protected void initProperties(Map<String, String> props) {
    }

    public void contextDestroyed(ServletContextEvent event) {
        if (logConfigurators != null && logConfigurators.length > 0) {
            ServletContext servletContext = event.getServletContext();
            servletContext.log("Shutting down log system");

            for (LogConfigurator logConfigurator : logConfigurators) {
                logConfigurator.shutdown();
            }
        }
    }

    private String getLogConfiguration(Map<String, String> params, String logSystem) {
        String logConfiguration = params.remove(LOG_CONFIGURATION);

        if (logConfiguration == null) {
            logConfiguration = "/WEB-INF/" + logSystem + ".xml";
        }

        return logConfiguration;
    }

    private String[] getLogSystems(Map<String, String> params) {
        String logSystem = params.remove(LOG_SYSTEM);

        if (logSystem == null) {
            return new String[0];
        } else {
            return logSystem.split("(,|\\s)+");
        }
    }

    /**
     * 取得所有以log开头的init params。
     */
    private Map<String, String> getLogInitParams(ServletContext servletContext) {
        Map<String, String> params = new HashMap<String, String>();

        for (Enumeration<?> i = servletContext.getInitParameterNames(); i.hasMoreElements();) {
            String name = (String) i.nextElement();

            if (name != null && name.startsWith(LOG_PREFIX)) {
                String value = trimToNull(servletContext.getInitParameter(name));

                if (value != null) {
                    params.put(name, value);
                }
            }
        }

        return params;
    }
}
