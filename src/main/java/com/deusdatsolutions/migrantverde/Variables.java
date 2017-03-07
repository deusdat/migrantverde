/*
 * Copyright 2017 DeusDat Solutions Corp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.deusdatsolutions.migrantverde;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Wrap up the system and the properties for the JVM. Makes it easier to get
 * variables that way.
 * 
 * @author J Patrick Davenport
 *
 */
public class Variables {
	private final Map<String, String> values = new HashMap<>(System.getenv());

	public Variables() {
		final Set<Entry<Object, Object>> entrySet = System.getProperties().entrySet();
		for ( final Entry<Object, Object> entry : entrySet ) {
			values.put(	entry.getKey().toString(),
						entry.getValue().toString());
		}
	}

	public String get( final String key ) {
		return values.get(key);
	}

	public Set<Entry<String, String>> keyValues() {
		return values.entrySet();
	}
}
