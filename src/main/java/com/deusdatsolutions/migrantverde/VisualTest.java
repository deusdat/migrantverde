package com.deusdatsolutions.migrantverde;

import java.nio.file.Path;
import java.util.SortedSet;

/**
 * A simple test that allows the developer of the project to see that migration
 * finding works even when the resources are in a jar.
 * 
 * @author J Patrick Davenport
 *
 */
public class VisualTest {

	public static void main(final String[] args) {
		final MigrationsFinder mf = new MigrationsFinder();
		final SortedSet<Path> migrations = mf.migrations("/visualTesting");
		if (migrations.size() != 3) {
			System.out.println("Didn't find the files I expected");
			System.exit(1);
		} else {
			System.out.println("Looks good, boss. Stack Overflow was right. Why doubt?");
		}
	}

}
