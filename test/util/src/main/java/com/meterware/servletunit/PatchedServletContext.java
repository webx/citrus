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

package com.meterware.servletunit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

public class PatchedServletContext extends ServletUnitServletContext {
    private final WebApplication webapp;

    public PatchedServletContext(WebApplication application) {
        super(application);
        this.webapp = application;
    }

    @Override
    public URL getResource(String path) {
        URL res = super.getResource(path);

        if (res != null) {
            InputStream is = null;

            try {
                is = res.openStream();
            } catch (IOException e) {
                return null;
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        return res;
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        File root = getWebApplication().getResourceFile(path);
        Set<String> paths = new TreeSet<String>();
        File[] files = root.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    paths.add(path + file.getName() + "/");
                } else {
                    paths.add(path + file.getName());
                }
            }
        }

        return paths;
    }

    public WebApplication getWebApplication() {
        return webapp;
    }
}
