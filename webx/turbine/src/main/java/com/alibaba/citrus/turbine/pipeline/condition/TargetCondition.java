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
package com.alibaba.citrus.turbine.pipeline.condition;

/**
 * ¸ù¾ÝtargetÀ´ÅÐ¶Ï¡£
 * 
 * @author Michael Zhou
 */
public class TargetCondition extends PathCondition {
    @Override
    protected String getPath() {
        return getRunData().getTarget();
    }

    @Override
    protected void log(String patternString) {
        log.debug("Target matched pattern: {}", patternString);
    }

    public static class DefinitionParser extends AbstractPathConditionDefinitionParser<TargetCondition> {
    }
}
