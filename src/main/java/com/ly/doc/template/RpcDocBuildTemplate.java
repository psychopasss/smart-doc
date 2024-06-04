/*
 * Copyright (C) 2018-2024 smart-doc
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.ly.doc.template;

import com.ly.doc.builder.ProjectDocConfigBuilder;
import com.ly.doc.constants.DocTags;
import com.ly.doc.constants.DubboAnnotationConstants;
import com.ly.doc.model.*;
import com.ly.doc.model.annotation.FrameworkAnnotations;
import com.ly.doc.model.rpc.RpcApiDoc;
import com.ly.doc.utils.*;
import com.power.common.util.StringUtil;
import com.power.common.util.ValidateUtil;
import com.thoughtworks.qdox.model.*;
import com.thoughtworks.qdox.model.expression.AnnotationValue;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * @author yu 2020/1/29.
 */
public class RpcDocBuildTemplate implements IDocBuildTemplate<RpcApiDoc>, IWebSocketDocBuildTemplate<WebSocketDoc>, IRpcDocTemplate {


    @Override
    public boolean addMethodModifiers() {
        return false;
    }

    /**
     * api index
     */
    private final AtomicInteger atomicInteger = new AtomicInteger(1);

    @Override
    public List<RpcApiDoc> renderApi(ProjectDocConfigBuilder projectBuilder, Collection<JavaClass> candidateClasses) {
        ApiConfig apiConfig = projectBuilder.getApiConfig();
        List<RpcApiDoc> apiDocList = new ArrayList<>();
        int order = 0;
        boolean setCustomOrder = false;
        for (JavaClass cls : candidateClasses) {
            if (skipClass(apiConfig, cls, null)) {
                continue;
            }
            String strOrder = JavaClassUtil.getClassTagsValue(cls, DocTags.ORDER, Boolean.TRUE);
            order++;
            if (ValidateUtil.isNonNegativeInteger(strOrder)) {
                order = Integer.parseInt(strOrder);
                setCustomOrder = true;
            }
            List<RpcJavaMethod> apiMethodDocs = (List<RpcJavaMethod>) buildServiceMethod(cls, apiConfig, projectBuilder);
            this.handleJavaApiDoc(cls, apiDocList, apiMethodDocs, order, projectBuilder);
        }
        // sort
        if (apiConfig.isSortByTitle()) {
            Collections.sort(apiDocList);
        } else if (setCustomOrder) {
            // while set custom oder
            return apiDocList.stream()
                    .sorted(Comparator.comparing(RpcApiDoc::getOrder))
                    .peek(p -> p.setOrder(atomicInteger.getAndAdd(1))).collect(Collectors.toList());
        }
        return apiDocList;
    }

    @Override
    public List<WebSocketDoc> renderWebSocketApi(ProjectDocConfigBuilder projectBuilder, Collection<JavaClass> candidateClasses) {
        return null;
    }

    @Override
    public boolean ignoreReturnObject(String typeName, List<String> ignoreParams) {
        return false;
    }


    @Override
    public boolean isEntryPoint(JavaClass cls, FrameworkAnnotations frameworkAnnotations) {
        // Exclude DubboSwaggerService from dubbo 2.7.x
        if (DubboAnnotationConstants.DUBBO_SWAGGER.equals(cls.getCanonicalName())) {
            return false;
        }
        List<JavaAnnotation> classAnnotations = cls.getAnnotations();
        for (JavaAnnotation annotation : classAnnotations) {
            String name = annotation.getType().getCanonicalName();
            if (DubboAnnotationConstants.SERVICE.equals(name)
                    || DubboAnnotationConstants.DUBBO_SERVICE.equals(name)
                    || DubboAnnotationConstants.ALI_DUBBO_SERVICE.equals(name)) {
                return true;
            }
        }
        List<DocletTag> docletTags = cls.getTags();
        for (DocletTag docletTag : docletTags) {
            String value = docletTag.getName();
            if (DocTags.DUBBO.equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public FrameworkAnnotations registeredAnnotations() {
        return null;
    }

    private void handleJavaApiDoc(JavaClass cls, List<RpcApiDoc> apiDocList, List<RpcJavaMethod> apiMethodDocs,
                                  int order, ProjectDocConfigBuilder builder) {
        String className = cls.getCanonicalName();
        String shortName = cls.getName();
        String comment = cls.getComment();
        List<JavaType> javaTypes = cls.getImplements();
        if (!javaTypes.isEmpty() && !cls.isInterface()) {
            JavaType javaType = javaTypes.get(0);
            className = javaType.getCanonicalName();
            shortName = className;
            JavaClass javaClass = builder.getClassByName(className);
            if (StringUtil.isEmpty(comment) && Objects.nonNull(javaClass)) {
                comment = javaClass.getComment();
            }
        }
        RpcApiDoc apiDoc = new RpcApiDoc();
        apiDoc.setOrder(order);
        apiDoc.setName(className);
        apiDoc.setShortName(shortName);
        apiDoc.setAlias(className);
        apiDoc.setUri(builder.getServerUrl() + "/" + className);
        apiDoc.setProtocol("dubbo");
        if (builder.getApiConfig().isMd5EncryptedHtmlName()) {
            String name = DocUtil.generateId(apiDoc.getName());
            apiDoc.setAlias(name);
        }
        apiDoc.setDesc(DocUtil.getEscapeAndCleanComment(comment));
        apiDoc.setList(apiMethodDocs);

        List<JavaAnnotation> annotations = cls.getAnnotations();
        for (JavaAnnotation annotation : annotations) {
            String name = annotation.getType().getCanonicalName();
            if (!DubboAnnotationConstants.DUBBO_SERVICE.equals(name)) {
                continue;
            }
            AnnotationValue versionValue = annotation.getProperty("version");
            if (Objects.nonNull(versionValue)) {
                apiDoc.setVersion(StringUtil.removeDoubleQuotes(versionValue.getParameterValue().toString()));
            }
            AnnotationValue protocolValue = annotation.getProperty("protocol");
            if (Objects.nonNull(protocolValue)) {
                apiDoc.setProtocol(StringUtil.removeDoubleQuotes(protocolValue.getParameterValue().toString()));
            }
            AnnotationValue interfaceNameValue = annotation.getProperty("interfaceName");
            if (Objects.nonNull(interfaceNameValue)) {
                apiDoc.setName(StringUtil.removeDoubleQuotes(interfaceNameValue.getParameterValue().toString()));
            }
        }
        List<DocletTag> docletTags = cls.getTags();
        List<String> authorList = new ArrayList<>();
        for (DocletTag docletTag : docletTags) {
            String name = docletTag.getName();
            if (DocTags.VERSION.equals(name)) {
                apiDoc.setVersion(docletTag.getValue());
            }
            if (DocTags.AUTHOR.equals(name)) {
                authorList.add(docletTag.getValue());
            }
            // set rpc protocol
            if (DocTags.PROTOCOL.equals(name)) {
                apiDoc.setProtocol(docletTag.getValue());
            }
            // set rpc service name
            if (DocTags.SERVICE.equals(name)) {
                apiDoc.setName(docletTag.getValue());
            }
        }
        apiDoc.setAuthor(String.join(", ", authorList));
        apiDocList.add(apiDoc);
    }
}
