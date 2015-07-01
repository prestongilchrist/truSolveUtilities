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
import java.util.Map;

import javax.script.ScriptContext;

import org.apache.tools.ant.filters.BaseParamFilterReader;
import org.apache.tools.ant.types.Parameter;

import com.trusolve.io.ScriptFilter;
import com.trusolve.io.ScriptTemplateCodeGenerator;

/**
 * @author Preston Gilchrist
 *
 */
public class ScriptTemplateFilter
	extends BaseParamFilterReader
{
	private ScriptTemplateCodeGenerator stcg;
	private ScriptFilter sf;
	
	@SuppressWarnings("unused")
	private static final String CLASS_ID = "$Id$";

	private boolean initialized = false;
	
	public ScriptTemplateFilter( Reader in )
		throws IOException
	{
		this( new ScriptTemplateCodeGenerator(in) );
	}
	
	private ScriptTemplateFilter( ScriptTemplateCodeGenerator stcg )
		throws IOException
	{
		this( new ScriptFilter( stcg ) );
		this.stcg = stcg;
	}

	private ScriptTemplateFilter( ScriptFilter sf )
	{
		super( sf );
		this.sf = sf;
	}
	
	public int read()
		throws IOException
	{
		if( ! initialized )
		{
			initialize();
		}
		return(super.read());
	}
	public void initialize()
	{
		sf.setAttribute("project", getProject(), ScriptContext.GLOBAL_SCOPE);
		for( Parameter p : this.getParameters() )
		{
			if( p.getName().equals( "scriptType" ) )
			{
				stcg.setScriptType( p.getValue() );
				sf.setEngineName( p.getValue() );
			}
			else if( p.getName().equals("loadBindingsRef") )
			{
				Object o = getProject().getReference(p.getValue());
				if( o instanceof Map<?,?> )
				{
					sf.putAll((Map<String,Object>)o);
				}
			}
			else if( ! p.getName().equals( "project" ) )
			{
				sf.setAttribute( p.getName(), p.getValue(), ( p.getType().equals( "global" ) ? ScriptContext.GLOBAL_SCOPE : ScriptContext.ENGINE_SCOPE ) );
			}
		}
		initialized=true;
	}
}
