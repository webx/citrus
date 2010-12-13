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
package com.alibaba.citrus.turbine.uribroker.uri;

import static com.alibaba.citrus.turbine.TurbineConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import com.alibaba.citrus.service.mappingrule.MappingRuleService;
import com.alibaba.citrus.service.uribroker.uri.URIBroker;
import com.alibaba.citrus.util.StringUtil;

/**
 * Turbine风格的URI。
 * <p>
 * 一个Turbine风格的URI包括如下几个部分：
 * </p>
 * 
 * <pre>
 * URI         = SERVER_INFO + PATH + "?" + QUERY_DATA + "#" + REFERENCE
 * SERVER_INFO = scheme://loginUser:loginPassword@serverName:serverPort
 * PATH        = /contextPath/servletPath/PATH_INFO
 * PATH_INFO   = /componentPath/target
 * QUERY_DATA  = queryKey1=value1&queryKey2=value2
 * REFERENCE   = reference
 * </pre>
 * <p>
 * 例如：
 * </p>
 * 
 * <pre>
 * http://user:pass@myserver.com:8080/mycontext/myservlet/mycomponent/myTarget.jhtml
 * </pre>
 * 
 * @author Michael Zhou
 */
public class TurbineURIBroker extends WebxURIBroker {
    protected static final int TARGET_INDEX = COMPONENT_PATH_INDEX + 1;
    private static final boolean DEFAULT_CONVERT_TARGET_CASE = true;
    private static final String DEFAULT_TARGET_MAPPING_RULE = EXTENSION_OUTPUT;
    private static final String DEFAULT_ACTION_PARAM = "action";
    private MappingRuleService mappingRuleService;
    private boolean hasTarget;
    private String target;
    private Boolean convertTargetCase;
    private String targetMappingRule;
    private String actionParam;

    public MappingRuleService getMappingRuleService() {
        return mappingRuleService;
    }

    public void setMappingRuleService(MappingRuleService mappingRuleService) {
        this.mappingRuleService = mappingRuleService;
    }

    public String getTarget() {
        if (hasTarget) {
            return target;
        } else {
            return null;
        }
    }

    public TurbineURIBroker setTarget(String target) {
        setPathSegment(TARGET_INDEX, convertTarget(target));
        this.hasTarget = true;
        this.target = trimToNull(target);
        return this;
    }

    private String convertTarget(String target) {
        target = trimToNull(target);

        if (target != null) {
            // 将target映射成外部形式。
            if (getMappingRuleService() != null) {
                target = getMappingRuleService().getMappedName(getTargetMappingRule(), target);
            }

            if (!isEmpty(target)) {
                if (isConvertTargetCase()) {
                    // 将target转换成target_name形式。
                    int lastSlashIndex = target.lastIndexOf("/");

                    if (lastSlashIndex >= 0) {
                        target = target.substring(0, lastSlashIndex) + "/"
                                + StringUtil.toLowerCaseWithUnderscores(target.substring(lastSlashIndex + 1));
                    } else {
                        target = StringUtil.toLowerCaseWithUnderscores(target);
                    }
                }
            }
        }

        return target;
    }

    public String getTargetMappingRule() {
        return defaultIfNull(targetMappingRule, DEFAULT_TARGET_MAPPING_RULE);
    }

    public void setTargetMappingRule(String targetMappingRule) {
        this.targetMappingRule = trimToNull(targetMappingRule);
    }

    public boolean isConvertTargetCase() {
        return convertTargetCase == null ? DEFAULT_CONVERT_TARGET_CASE : convertTargetCase.booleanValue();
    }

    public TurbineURIBroker setConvertTargetCase(boolean convertTargetCase) {
        this.convertTargetCase = convertTargetCase;
        return this;
    }

    public String getActionParam() {
        return defaultIfNull(actionParam, DEFAULT_ACTION_PARAM);
    }

    public TurbineURIBroker setActionParam(String actionParam) {
        this.actionParam = trimToNull(actionParam);
        return this;
    }

    public String getAction() {
        return getQueryData(getActionParam());
    }

    public TurbineURIBroker setAction(String action) {
        setQueryData(getActionParam(), trimToNull(action));
        return this;
    }

    @Override
    protected TurbineURIBroker newInstance() {
        return new TurbineURIBroker();
    }

    @Override
    protected void initDefaults(URIBroker parent) {
        super.initDefaults(parent);

        if (parent instanceof TurbineURIBroker) {
            TurbineURIBroker parentTurbine = (TurbineURIBroker) parent;

            if (mappingRuleService == null) {
                mappingRuleService = parentTurbine.mappingRuleService;
            }

            if (convertTargetCase == null) {
                convertTargetCase = parentTurbine.convertTargetCase;
            }

            if (targetMappingRule == null) {
                targetMappingRule = parentTurbine.targetMappingRule;
            }

            if (actionParam == null) {
                actionParam = parentTurbine.actionParam;
            }

            if (!hasTarget && parentTurbine.hasTarget) {
                setTarget(parentTurbine.target);
            }

            if (parentTurbine.getAction() != null) {
                getQuery().remove(parentTurbine.getActionParam());

                if (getAction() == null) {
                    setAction(parentTurbine.getAction());
                }
            }
        }
    }

    @Override
    protected void copyFrom(URIBroker parent) {
        super.copyFrom(parent);

        if (parent instanceof TurbineURIBroker) {
            TurbineURIBroker parentTurbine = (TurbineURIBroker) parent;

            mappingRuleService = parentTurbine.mappingRuleService;
            convertTargetCase = parentTurbine.convertTargetCase;
            targetMappingRule = parentTurbine.targetMappingRule;
            actionParam = parentTurbine.actionParam;

            if (hasTarget) {
                target = null;
                clearPathSegment(TARGET_INDEX);
            }

            if (parentTurbine.hasTarget) {
                setTarget(parentTurbine.target);
            }

            if (getAction() != null) {
                removeQueryData(getActionParam());
            }

            if (parentTurbine.getAction() != null) {
                setAction(parentTurbine.getAction());
            }
        }
    }

    @Override
    protected int getPathSegmentCount() {
        return 4;
    }
}
