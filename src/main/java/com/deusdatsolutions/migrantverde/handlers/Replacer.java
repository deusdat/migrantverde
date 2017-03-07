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
