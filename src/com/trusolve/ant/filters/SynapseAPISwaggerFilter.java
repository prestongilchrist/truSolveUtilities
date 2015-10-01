/**
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version 
 * 1.1 (the "License"); you may not use this file except in compliance with 
 * the License. You may obtain a copy of the License at 
 * http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 * 
 * The Original Code is truSolve Utilities.
 * 
 * The Initial Developer of the Original Code is
 * truSolve.com.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * ***** END LICENSE BLOCK *****
 *
 * $Id$
 *
 */

package com.trusolve.ant.filters;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.script.ScriptContext;

import org.apache.tools.ant.filters.BaseParamFilterReader;
import org.apache.tools.ant.types.Parameter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.trusolve.json.YamlToJson;

/**
 * @author Preston Gilchrist
 *
 */
public class SynapseAPISwaggerFilter
	extends BaseParamFilterReader
{
	
	@SuppressWarnings("unused")
	private static final String CLASS_ID = "$Id$";

	public SynapseAPISwaggerFilter( Reader in )
		throws IOException
	{
		super(readDocument(in));
	}
	
	
	public int read()
		throws IOException
	{
		return super.read();
	}
	
	
	public static Reader readDocument(Reader in)
		throws JsonProcessingException, IOException
	{
		ObjectMapper jsonIn = new ObjectMapper();
		JsonNode jn = jsonIn.readTree(in);
		
		fixGetPaths(jn);
		
		jsonIn.enable(SerializationFeature.INDENT_OUTPUT);
		
		return new StringReader(jsonIn.writeValueAsString(jn));
	}
	
	private static void fixGetPaths(JsonNode root)
	{
		Map<String,JsonNode> toAdd = new HashMap<String,JsonNode>();
		JsonNode paths = root.get("paths");
		if( paths != null && paths.isObject() )
		{
			ObjectNode pathsObject = (ObjectNode)paths;
			for( Iterator<Map.Entry<String,JsonNode>> i = paths.fields() ; i.hasNext() ; )
			{
				Map.Entry<String, JsonNode> e = i.next();
				String pathName = e.getKey();
				// Check to see if this a template type
				if( pathName != null && pathName.contains("{") )
				{
					JsonNode pathDetail = e.getValue();
					if( pathDetail != null && pathDetail.has("get") )
					{
						ObjectNode newPathDetail = pathDetail.deepCopy();
						newPathDetail.remove("put");
						newPathDetail.remove("post");
						newPathDetail.remove("delete");
						newPathDetail.remove("options");
						newPathDetail.remove("head");
						newPathDetail.remove("patch");
						
						String newPathName = pathName + "?*";
						
						toAdd.put(newPathName, newPathDetail);
					}
				}
			}
			for( Map.Entry<String, JsonNode> e : toAdd.entrySet() )
			{
				pathsObject.set(e.getKey(), e.getValue());
			}
		}
	}
}
