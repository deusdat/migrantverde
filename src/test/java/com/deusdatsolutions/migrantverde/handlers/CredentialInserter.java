package com.deusdatsolutions.migrantverde.handlers;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.BaseDocument;
import com.deusdatsolutions.migrantverde.ArangoDbFunction;

public class CredentialInserter implements ArangoDbFunction {

	@Override
	public void apply(final ArangoDriver driver) throws ArangoException {
		/*
		 * While this example is simple, think of how you can use this in production. For example, you need insert some
		 * system accounts, but you don't want to add the credentials to source control. Instead you can use environment
		 * variables.
		 */
		final BaseDocument value = new BaseDocument();
		value.addAttribute("name", "Wacko");
		value.addAttribute("username", "ringo");
		value.addAttribute("password", "starr");
		driver.createDocument("Users", value);
	}

}
