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

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleBindings;

import org.apache.tools.ant.filters.BaseFilterReader;
import org.apache.tools.ant.filters.ChainableReader;

/**
 * @author Preston Gilchrist
 * 
 */
public final class ScriptSubFilter
	extends BaseFilterReader
	implements ChainableReader
{
	@SuppressWarnings("unused")
	private static final String CLASS_ID = "$Id$";

	private com.trusolve.io.ScriptSubFilter ssf;
	private boolean initialized = false;

	public ScriptSubFilter(final Reader in)
	{
		super(in);
		ssf = new com.trusolve.io.ScriptSubFilter(in);
	}

	public Reader chain(final Reader in)
	{
		ScriptSubFilter newFilter = new ScriptSubFilter(in);
		newFilter.setProject(getProject());
		return( newFilter );
	}
	
	public int read()
		throws IOException
	{
		if( ! initialized ) init();
		return(ssf.read());
	}
	
	private void init()
	{
		Bindings sb = new SimpleBindings();
		sb.put("project", getProject());
		ssf.sc.setBindings(sb, ScriptContext.GLOBAL_SCOPE);
		initialized=true;
	}
}
