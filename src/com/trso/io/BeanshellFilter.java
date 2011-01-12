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

/**
 * This class implements a FilterReader that takes Beanshell code as input and outputs the
 * output results of executing the script.  The writer used by the script will be captured
 * in the ScriptContext and that data written to the writer by the executed script will be
 * the resulting data in the read operations of the script.
 */
package com.trso.io;

import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

/**
 * @author Preston Gilchrist
 *
 */
public class BeanshellFilter
	extends FilterReader
{
	@SuppressWarnings("unused")
	private static final String CLASS_ID = "$Id$";
	private ScriptEngine engine = new ScriptEngineManager().getEngineByName("bsh");
	private ScriptContext sc = new SimpleScriptContext();
	private PipedWriter shellWriter = new PipedWriter();
	private boolean init = false;
	private Reader scriptInput;
	private Exception scriptException = null;
	
	/**
	 * This a convenience method to allow command line execution of the Beanshell filter against a file.
	 * 
	 * @param args a list of files which to process with the Beanshell filter reader
	 */
	public static void main( String[] args )
	{
		try
		{
			Reader base;
			if( args.length > 0 )
			{
				base = new java.io.FileReader( args[0] );
			}
			else
			{
				base = new InputStreamReader( System.in );
			}
		
			BeanshellFilter bshf = new BeanshellFilter(new BeanshellTemplateCodeGen(base));
			for( int v = bshf.read(); v > -1 ; v = bshf.read() )
			{
				System.out.print((char)v);
			}
		}
		catch(Exception e)
		{
			System.err.println("Error: " + e.toString());
			e.printStackTrace(System.err);
		}
	}
	
	public BeanshellFilter( Reader in )
	{
		super(new PipedReader());
		scriptInput = in;
		try
		{
			shellWriter.connect((PipedReader)this.in);
			sc.setWriter(shellWriter);
			sc.setBindings(new SimpleBindings(), ScriptContext.GLOBAL_SCOPE);
		}
		catch( Exception e )
		{}
	}
	
	public int read()
		throws IOException
	{
		if( ! init )
		{
			try
			{
				initialize();
			}
			catch( Exception e )
			{
				throw( new IOException( "Script Exception", e ) );
			}
		}
		if( scriptException != null )
		{
			throw( new IOException( "Script Exception", scriptException ) );
		}
		return(in.read());
	}
	
	/* Methods to access the ScriptContext object */
	public Object getAttribute(String name)
	{
		return(sc.getAttribute(name));
	}
	public Object getAttribute(String name, int scope)
	{
		return(sc.getAttribute(name,scope));
	}
	public void setAttribute(String name, Object value, int scope)
	{
		sc.setAttribute(name, value, scope);
	}
	
	private void initialize()
		throws Exception
	{
		init = true;
		( 
			new Thread()
			{
				public void run()
				{
					try
					{
						engine.eval(scriptInput, sc);
					}
					catch( Exception e )
					{
						scriptException = e;
					}
					finally
					{
						try
						{
							shellWriter.flush();
							shellWriter.close();
						}
						catch( Exception e ){}
					}
				}
			}
		).start();
	}
}
