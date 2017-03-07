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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.deusdatsolutions.migrantverde.jaxb.MigrationType;

/**
 * Reads the configuration files. Enforces the XSD specified in the resources
 * directory. Replaces variables in XML.
 * 
 * @author J Patrick Davenport
 *
 */
public class Deserializier {
	private final JAXBContext ctx;
	private final Unmarshaller jaxbUnmarshaller;
	private final Variables variables;

	public Deserializier() {
		super();
		try {
			this.variables = new Variables();
			this.ctx = JAXBContext.newInstance("com.deusdatsolutions.migrantverde.jaxb");
			this.jaxbUnmarshaller = ctx.createUnmarshaller();

			final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			final Schema migrationSchema = schemaFactory.newSchema(this.getClass().getResource("/migrations.xsd"));

			jaxbUnmarshaller.setSchema(migrationSchema);
			jaxbUnmarshaller.setEventHandler(new DefaultValidationEventHandler());
		} catch ( final JAXBException | SAXException ex ) {
			throw new IllegalArgumentException("Can't create JAXB context", ex);
		}
	}

	@SuppressWarnings( "unchecked" )
	public MigrationType get( final Path xml ) {
		try {
			final InputStream xmlSrcStream = load(xml);
			final JAXBElement<MigrationType> unmarshal = (JAXBElement<MigrationType>) this.jaxbUnmarshaller
					.unmarshal(xmlSrcStream);
			return unmarshal.getValue();
		} catch ( final Exception ex ) {
			throw new MigrationException("Couldn't process path " + xml, ex);
		}
	}

	private InputStream load( final Path xml ) throws IOException {
		final InputStream xmlSrcStream = Files.newInputStream(	xml,
																StandardOpenOption.READ);
		final String replaceProperties = replaceProperties(xmlSrcStream);
		return new ByteArrayInputStream(replaceProperties.getBytes(StandardCharsets.UTF_8));
	}

	private String replaceProperties( final InputStream in ) throws IOException {
		String asString = migrationAsString(in);
		for ( final Entry<String, String> entry : variables.keyValues() ) {
			final String k = String.join(	"",
											"#",
											entry.getKey(),
											"#");
			asString = asString.replace(k,
										entry.getValue());
		}
		return asString;
	}

	private String migrationAsString( final InputStream in ) throws IOException {
		try (BufferedReader buf = new BufferedReader(new InputStreamReader(in)) ) {
			final StringBuilder sb = new StringBuilder();
			String line;
			while ( (line = buf.readLine()) != null ) {
				sb.append(line);
			}
			return sb.toString();
		} finally {
			in.close();
		}
	}

}
