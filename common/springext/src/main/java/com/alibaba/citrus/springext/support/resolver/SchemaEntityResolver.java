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
package com.alibaba.citrus.springext.support.resolver;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.Schemas;
import com.alibaba.citrus.springext.support.SchemaSet;

public class SchemaEntityResolver implements EntityResolver {
    private final static Logger log = LoggerFactory.getLogger(SchemaEntityResolver.class);
    private final EntityResolver defaultEntityResolver;
    private final SchemaSet schemas;

    public SchemaEntityResolver(EntityResolver defaultEntityResolver, Schemas... schemasList) {
        this.defaultEntityResolver = defaultEntityResolver;
        this.schemas = new SchemaSet(schemasList);
    }

    public EntityResolver getDefaultEntityResolver() {
        return defaultEntityResolver;
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        log.trace("Trying to locate XML entity {} as configuration points schema.", systemId);

        Schema schema = schemas.findSchema(systemId);

        if (schema == null) {
            if (defaultEntityResolver != null) {
                return defaultEntityResolver.resolveEntity(publicId, systemId);
            } else {
                return null;
            }
        }

        log.debug("Found XML schema for systemId {}: {}", systemId, schema);

        return new InputSource(schema.getInputStream());
    }
}
