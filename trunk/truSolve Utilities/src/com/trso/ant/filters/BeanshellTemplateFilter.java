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

package com.trso.ant.filters;

import java.io.IOException;
import java.io.Reader;

import javax.script.ScriptContext;

import org.apache.tools.ant.filters.BaseFilterReader;

import com.trso.io.BeanshellFilter;
import com.trso.io.BeanshellTemplateCodeGen;

/**
 * @author Preston Gilchrist
 *
 */
public class BeanshellTemplateFilter
	extends BaseFilterReader
{
	@SuppressWarnings("unused")
	private static final String CLASS_ID = "$Id$";

	private boolean initialized = false;
	
	public BeanshellTemplateFilter( Reader in )
	{
		super(new BeanshellFilter(new BeanshellTemplateCodeGen(in)));
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
		BeanshellFilter bshf = (BeanshellFilter)in;
		bshf.setAttribute("project", getProject(), ScriptContext.GLOBAL_SCOPE);
		initialized=true;
	}
}
