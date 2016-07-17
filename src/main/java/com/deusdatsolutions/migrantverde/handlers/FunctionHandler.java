package com.deusdatsolutions.migrantverde.handlers;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.deusdatsolutions.migrantverde.ArangoDbFunction;
import com.deusdatsolutions.migrantverde.jaxb.ArangoFunctionType;

/**
 * Converter that allows the user to run an arbitrary Java method against the database.
 * 
 * @author J Patrick Davenport
 *
 */
public class FunctionHandler implements IMigrationHandler<ArangoFunctionType> {

	@Override
	public void migrate(final ArangoFunctionType migration, final ArangoDriver driver) throws ArangoException {
		final String clazz = migration.getClazz();
		try {
			final Class<?> loadClass = getClass().getClassLoader().loadClass(clazz);
			final ArangoDbFunction newInstance = (ArangoDbFunction) loadClass.newInstance();
			newInstance.apply(driver);
		} catch (final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException("Can't find " + clazz, e);
		}
	}

}
