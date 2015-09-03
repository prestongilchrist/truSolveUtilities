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
import java.util.Map;

import javax.script.ScriptContext;

import org.apache.tools.ant.filters.BaseParamFilterReader;
import org.apache.tools.ant.types.Parameter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trusolve.json.YamlToJson;

/**
 * @author Preston Gilchrist
 *
 */
public class YamlToJsonFilter
	extends BaseParamFilterReader
{
	
	@SuppressWarnings("unused")
	private static final String CLASS_ID = "$Id$";

	public YamlToJsonFilter( Reader in )
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
		YamlToJson yj = new YamlToJson();
		String doc = yj.convertToString(in);
		return new StringReader(doc);
	}
}
