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

package com.alibaba.citrus.dev.handler.impl.visitor;

import static com.alibaba.citrus.dev.handler.util.DomUtil.*;
import static com.alibaba.citrus.util.ArrayUtil.*;
import static java.util.Collections.*;

import java.io.IOException;

import com.alibaba.citrus.dev.handler.component.DomComponent.ControlBarCallback;
import com.alibaba.citrus.dev.handler.impl.ExplorerHandler.ExplorerVisitor;
import com.alibaba.citrus.dev.handler.util.ConfigurationFile;
import com.alibaba.citrus.util.templatelite.Template;
import com.alibaba.citrus.webx.handler.RequestHandlerContext;

public class ConfigurationsVisitor extends AbstractFallbackVisitor<ExplorerVisitor> {
    private final ConfigurationFile[] configurationFiles;
    private       ConfigurationFile   configurationFile;

    public ConfigurationsVisitor(RequestHandlerContext context, ExplorerVisitor v,
                                 ConfigurationFile[] configurationFiles) {
        super(context, v);
        this.configurationFiles = configurationFiles;
    }

    public void visitConfigurations(Template withImportsTemplate, Template noImportsTemplate) throws IOException {
        for (ConfigurationFile configurationFile : configurationFiles) {
            this.configurationFile = configurationFile;

            if (isEmptyArray(configurationFile.getImportedFiles())) {
                noImportsTemplate.accept(this);
            } else {
                withImportsTemplate.accept(this);
            }
        }
    }

    public void visitConfigurationName() {
        out().print(configurationFile.getName());
    }

    public void visitConfigurationNameForId() {
        out().print(toId(configurationFile.getName()));
    }

    public void visitConfigurationUrl() {
        out().print(configurationFile.getUrl().toExternalForm());
    }

    public void visitImports(Template withImportsTemplate, Template noImportsTemplate) throws IOException {
        new ConfigurationsVisitor(context, getFallbackVisitor(), configurationFile.getImportedFiles())
                .visitConfigurations(withImportsTemplate, noImportsTemplate);
    }

    public void visitConfigurationContent(final Template controlBarTemplate) {
        getFallbackVisitor().getDomComponent().visitTemplate(context,
                                                             singletonList(configurationFile.getRootElement()), new ControlBarCallback() {
            public void renderControlBar() {
                controlBarTemplate.accept(ConfigurationsVisitor.this);
            }
        });
    }
}
