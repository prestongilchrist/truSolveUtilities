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

package com.trso.io;

import java.io.FilterReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * 
 * @author Preston Gilchrist
 *
 */
public class TeeFilter
	extends FilterReader
{
	@SuppressWarnings("unused")
	private static final String CLASS_ID = "$Id$";
	private Writer output = new OutputStreamWriter(System.out);
	
	public TeeFilter(Reader in)
	{
		super(in);
		// TODO Auto-generated constructor stub
	}

	public TeeFilter( Reader in, Writer output )
	{
		super(in);
		this.output = output; 
	}
	
	
	@SuppressWarnings("unused")
	private static final String CLASS_DATE = "$Date$";
	@SuppressWarnings("unused")
	private static final String CLASS_REVISION = "$Revision$";

	
	@Override
	public int read()
		throws IOException
	{
		int c = super.read();
		if( c != -1 )
		{
			output.write(c);
			output.flush();
		}
		return(c);
	}

	@Override
	public int read(char[] cbuf, int off, int len)
		throws IOException
	{
		int c = super.read(cbuf, off, len);
		if( c > 0 )
		{
			output.write(cbuf, off, len);
			output.flush();
		}
		return(c);
	}
}
