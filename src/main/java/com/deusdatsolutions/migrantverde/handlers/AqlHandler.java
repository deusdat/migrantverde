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

import java.util.Map;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;

import static com.deusdatsolutions.migrantverde.handlers.Replacer.*;

/**
 * Supports converting an AQL configuration into a realized executiong.
 * 
 * @author J Patrick Davenport
 *
 */
public class AqlHandler implements IMigrationHandler<String> {
	private final Map<String, String> lookup;

	public AqlHandler( Map<String, String> lookup ) {
		this.lookup = lookup;
	}

	@Override
	public void migrate( final String migration, final ArangoDriver driver ) throws ArangoException {
		driver.executeAqlQuery(	replaceAll(	migration,
											lookup),
								null,
								null,
								null);
	}

}
