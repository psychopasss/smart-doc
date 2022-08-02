/*
 * smart-doc https://github.com/shalousun/smart-doc
 *
 * Copyright (C) 2018-2021 smart-doc
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
package com.power.doc.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.power.common.util.StringUtil;
import com.power.doc.constants.DocTags;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;

/**
 * @author yu 2021/6/26.
 */
public class JsonUtil {

    /**
     * Convert a JSON string to pretty print
     *
     * @param jsonString json string
     *
     * @return Format json string
     *
     * @author asdfs
     * @since 2022-05-27 17:46:49
     */
    public static String toPrettyFormat(String jsonString) {
        try {
            JsonElement jsonElement = JsonParser.parseString(jsonString);
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            String prettyJson = gson.toJson(jsonElement);
            return prettyJson;
        } catch (Exception e) {
            return jsonString;
        }
    }

    public static void main(String[] args) throws IOException {
        String asd = "sadfs";
        System.out.println(toPrettyFormat(asd));
        JavaProjectBuilder javaProjectBuilder = new JavaProjectBuilder();
        // 添加 java 源文件
        javaProjectBuilder.addSource(
                new File("/Users/yanzhenyu/Local/gitee/smart-doc/src/main/java/com/power/doc/utils/JsonUtil.java"));

        // 获得解析后的类
        Collection<JavaClass> classes = javaProjectBuilder.getClasses();
        for (JavaClass javaClass : classes) {
            for (JavaMethod method : javaClass.getMethods()) {
                String normalTagComments = DocUtil.getNormalTagComments(method, DocTags.SINCE, javaClass.getName());
                System.out.println(normalTagComments);
                // Map<String, String> paramsComments = (Map<String, String>)normalTagComments;
                // String authorValue = String.join(", ", new ArrayList<>(paramsComments.keySet()));
                // if (StringUtil.isNotEmpty(authorValue)) {
                //     String x = JsonUtil.toPrettyFormat(authorValue);
                //     System.out.println(x);
                // }
            }
        }
    }

    /**
     * Convert a JSON to String and pretty print
     *
     * @param src Json
     *
     * @return Format json string
     */
    public static String toPrettyJson(Object src) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(src);
        return prettyJson;
    }
}
