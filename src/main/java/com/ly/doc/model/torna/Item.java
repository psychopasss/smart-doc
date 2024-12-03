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
package com.ly.doc.model.torna;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * Torna Enum Item.
 *
 * @author xingzi 2021/2/25 12:29
 * @since 2.0.9
 **/
public class Item implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -1517636497626246584L;

	/**
	 * { * "name": "WAIT_PAY", * "type": "string", * "value": "0", * "description": "未支付"
	 * * }
	 */
	private String name;

	/**
	 * string, number, boolean, object, array
	 */
	private String type;

	/**
	 * value
	 */
	private String value;

	/**
	 * description
	 */
	private String description;

	public Item() {
	}

	public Item(String name, String type, String value, String description) {
		this.name = name;
		this.type = type;
		this.value = value;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
