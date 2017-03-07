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
package com.deusdatsolutions.migrantverde.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

public class Replacer {
	private Replacer() {

	}

	public static String replaceAll( String input, Map<String, String> lookup ) {
		if ( StringUtils.isEmpty(input) ) {
			return input;
		}
		String s = input;
		Map<String, String> mylookup = lookup == null ? new HashMap<String, String>() : lookup;
		for ( Entry<String, String> pair : mylookup.entrySet() ) {
			s = s.replace(	"${" + pair.getKey() + "}",
							pair.getValue());
		}
		return s;
	}
}
