package com.deusdatsolutions.migrantverde;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Wrap up the system and the properties for the JVM. Makes it easier to get variables that way.
 * 
 * @author J Patrick Davenport
 *
 */
public class Variables {
	private final Map<String, String> values = new HashMap<>(System.getenv());

	public Variables() {
		final Set<Entry<Object, Object>> entrySet = System.getProperties().entrySet();
		for (final Entry<Object, Object> entry : entrySet) {
			values.put(entry.getKey().toString(), entry.getValue().toString());
		}
	}

	public String get(final String key) {
		return values.get(key);
	}

	public Set<Entry<String, String>> keyValues() {
		return values.entrySet();
	}
}
