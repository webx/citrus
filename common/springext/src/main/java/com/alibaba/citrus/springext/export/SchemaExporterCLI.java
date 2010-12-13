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
package com.alibaba.citrus.springext.export;

import static com.alibaba.citrus.util.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.springframework.core.io.ResourceLoader;

import com.alibaba.citrus.logconfig.LogConfigurator;
import com.alibaba.citrus.springext.Schemas;

/**
 * 在命令行上调用<code>SchemaExporter</code>，可以把schema输出到文件中。
 * 
 * @author Michael Zhou
 */
public class SchemaExporterCLI extends SchemaExporter {
    public static void main(String[] args) throws IOException {
        File destdir = null;
        String uriPrefix = null;
        boolean debug = false;
        boolean toMoreArgs = false;

        for (String arg : args) {
            if ("-debug".equals(arg)) {
                debug = true;
            } else if (destdir == null) {
                destdir = new File(arg);
            } else if (uriPrefix == null) {
                uriPrefix = arg;
            } else {
                toMoreArgs = true;
            }
        }
        if (destdir == null || toMoreArgs) {
            System.err.println("Usage: SchemaExport [-debug] <directoryToExport> [uriPrefix]");
            System.exit(1);
        }

        LogConfigurator.getConfigurator().configureDefault(debug);

        SchemaExporterCLI exporter = new SchemaExporterCLI();

        if (uriPrefix == null || uriPrefix.length() == 0) {
            exporter.saveTo(destdir);
        } else {
            exporter.saveTo(destdir, uriPrefix);
        }
    }

    public SchemaExporterCLI() {
        super();
    }

    public SchemaExporterCLI(ResourceLoader resourceLoader) {
        super(resourceLoader);
    }

    public SchemaExporterCLI(Schemas... schemasList) {
        super(schemasList);
    }

    public void saveTo(File dir) throws IOException {
        saveTo(dir, dir.toURI().toString());
    }

    public void saveTo(File dir, String uriPrefix) throws IOException {
        if (uriPrefix == null) {
            log.info("Saving schema to directory: {}", dir.getAbsolutePath());
        } else {
            log.info("Saving schema to directory: \"{}\", with prefix: \"{}\"", dir.getAbsolutePath(), uriPrefix);
        }

        saveTo(dir, getRootEntry(), uriPrefix);
    }

    private void saveTo(File file, Entry entry, String uriPrefix) throws IOException {
        assertNotNull(file, "file");

        if (entry.isDirectory()) {
            if (!file.exists()) {
                file.mkdirs();
                log.debug("mkdir {}", file.getAbsolutePath());
            }

            if (!file.isDirectory() || !file.canWrite()) {
                throw new IOException("Exporting target is not a writable directory: " + file.getAbsolutePath());
            }

            for (Entry subEntry : entry.getSubEntries()) {
                saveTo(new File(file, subEntry.getName()), subEntry, uriPrefix);
            }
        } else {
            log.debug("Writing file: {}", file.getAbsolutePath());
            writeTo(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"), entry, "UTF-8", uriPrefix);
        }
    }
}
