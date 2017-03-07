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

/**
 * Component that searches the path for children documents.
 * 
 * @author J Patrick Davenport
 *
 */
public class MigrationsFinder {
	public SortedSet<Path> migrations( final String root, final Action action ) {
		final TreeSet<Path> result = new TreeSet<Path>();
		try {
			final URI uri = root.startsWith("file://") ? new URI(root)
					: MigrationsFinder.class.getResource(root).toURI();

			Path myPath;
			if ( uri.getScheme().equals("jar") ) {
				final FileSystem fileSystem = FileSystems.newFileSystem(uri,
																		Collections.<String, Object>emptyMap());
				myPath = fileSystem.getPath(root);
			} else {
				myPath = Paths.get(uri);
			}
			try (final Stream<Path> walk = Files.walk(	myPath,
														1) ) {
				for ( final Iterator<Path> it = walk.iterator(); it.hasNext(); ) {
					final Path possible = it.next();
					if ( possible.toString().endsWith(".xml") ) {
						result.add(possible);
					}
				}
			}
		} catch ( final Exception ex ) {
			throw new IllegalArgumentException("Could not load resources from " + root, ex);
		}
		return action == Action.MIGRATION ? result : result.descendingSet();
	}
}
