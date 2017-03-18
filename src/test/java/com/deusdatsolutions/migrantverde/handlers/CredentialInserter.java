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

import com.arangodb.entity.BaseDocument;
import com.deusdatsolutions.migrantverde.ArangoDbFunction;
import com.deusdatsolutions.migrantverde.DBContext;

public class CredentialInserter implements ArangoDbFunction {

	@Override
	public void apply( final DBContext driver ) {
		/*
		 * While this example is simple, think of how you can use this in
		 * production. For example, you need insert some system accounts, but
		 * you don't want to add the credentials to source control. Instead you
		 * can use environment variables.
		 */
		final BaseDocument value = new BaseDocument();
		value.addAttribute(	"name",
							"Wacko");
		value.addAttribute(	"username",
							"ringo");
		value.addAttribute(	"password",
							"starr");
		driver.db.collection("Users").insertDocument(value);
	}

}
