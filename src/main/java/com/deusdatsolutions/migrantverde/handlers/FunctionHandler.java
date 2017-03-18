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

import com.deusdatsolutions.migrantverde.ArangoDbFunction;
import com.deusdatsolutions.migrantverde.DBContext;
import com.deusdatsolutions.migrantverde.jaxb.ArangoFunctionType;

/**
 * Converter that allows the user to run an arbitrary Java method against the
 * database.
 * 
 * @author J Patrick Davenport
 *
 */
public class FunctionHandler implements IMigrationHandler<ArangoFunctionType> {

	@Override
	public void migrate( final ArangoFunctionType migration, final DBContext ctx ) {
		final String clazz = migration.getClazz();
		try {
			final Class<?> loadClass = getClass().getClassLoader().loadClass(clazz);
			final ArangoDbFunction newInstance = (ArangoDbFunction) loadClass.newInstance();
			newInstance.apply(ctx);
		} catch ( final ClassNotFoundException | InstantiationException | IllegalAccessException e ) {
			throw new IllegalArgumentException("Can't find " + clazz, e);
		}
	}

}
