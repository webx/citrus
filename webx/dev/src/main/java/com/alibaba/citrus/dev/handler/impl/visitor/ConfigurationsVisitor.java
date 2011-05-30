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
    private ConfigurationFile configurationFile;

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
