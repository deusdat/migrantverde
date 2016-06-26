package com.deusdatsolutions.migrantverde;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

public class MigrationsFinder {
	public SortedSet<Path> migrations(final String root) {
		final TreeSet<Path> result = new TreeSet<Path>();
		try {
			final URI uri = MigrationsFinder.class.getResource(root).toURI();
			Path myPath;
			if (uri.getScheme().equals("jar")) {
				final FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object> emptyMap());
				myPath = fileSystem.getPath(root);
			} else {
				myPath = Paths.get(uri);
			}
			try (final Stream<Path> walk = Files.walk(myPath, 1)) {
				for (final Iterator<Path> it = walk.iterator(); it.hasNext();) {
					final Path possible = it.next();
					if (possible.toString().endsWith(".xml")) {
						result.add(possible);
					}
				}
			}
		} catch (final Exception ex) {
			throw new IllegalArgumentException("Could not load resources from " + root, ex);
		}
		return result;
	}
}
