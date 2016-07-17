package com.deusdatsolutions.migrantverde;

/**
 * General runtime exception for migration errors. Helpful for wrapping the ArangoDB Driver's typed exceptions.
 * 
 * @author J Patrick Davenport
 *
 */
public class MigrationException extends RuntimeException {
	private static final long serialVersionUID = -3908108731490487055L;

	public MigrationException(	final String string,
								final Exception ex) {
		super(string, ex);
	}

}
