package com.deusdatsolutions.migrantverde;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Integration test to make sure that the system can merge environment variables
 * and properties passed to the JVM.
 * 
 * <p>
 * Environment Variables Required
 * <ul>
 * <li>ARANGODB_URL: http://somewherenice.com</li>
 * <li>ENV1: kitty</li>
 * </ul>
 * 
 * Properties
 * <ul>
 * <li>Prop1: timmy</li>
 * <li>Prop8: Rights for human like robots</li>
 * </ul>
 * </p>
 * 
 * @author J Patrick Davenport
 *
 */
public class VariableTest {

	final Variables v = new Variables();

	@Test
	public void env() {

		final String arangoDB = v.get("ARANGODB_URL");
		assertEquals(	"http://somewherenice.com",
						arangoDB);
		final String env1 = v.get("ENV1");
		assertEquals(	"kitty",
						env1);
	}

	@Test
	public void properties() {
		final String prop1 = v.get("Prop1");
		assertEquals(	"timmy",
						prop1);
		final String prop2 = v.get("Prop8");
		assertEquals(	"Rights for human like robots",
						prop2);
	}
}
