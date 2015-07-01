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

package com.trusolve.io;

import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

import com.trusolve.util.StringUtil;

/**
 * @author Preston Gilchrist
 * 
 */
public class ScriptSubFilter
	extends FilterReader
{
	@SuppressWarnings("unused")
	private static final String CLASS_ID = "$Id$";

	private ScriptEngineManager mgr = new ScriptEngineManager();
	public ScriptContext sc = new SimpleScriptContext();
	private final String scriptFilterTag = "scriptfilter";
	private String defaultScriptType = "Javascript";
	private StringBuffer readBuffer = new StringBuffer();
	private PipedReader scriptOutput = null;
	private Exception scriptException = null;

	private class ScriptExecutor
		extends Thread
	{
		private String scriptType;
		private Reader in;
		private PipedWriter output;
		
		ScriptExecutor( String scriptType, Reader in, PipedReader output )
			throws IOException
		{
			super();
			this.scriptType = scriptType;
			this.in = in;
			this.output = new PipedWriter(output);
		}
		
		public void run()
		{
			PrintStream iocapture = null;
			try
			{
				ScriptEngine engine = mgr.getEngineByName(scriptType);
				sc.setWriter(output);
				engine.eval(new ScriptReader(in), sc);
			}
			catch( Exception e )
			{
				scriptException = e;
			}
			finally
			{
				if( iocapture != null )
				{
					iocapture.flush();
					iocapture.close();
				}
				try
				{
					output.close();
				}
				catch( Exception e ){}
			}
		}
	}

	private class ScriptReader
		extends FilterReader
	{
		private StringBuffer scriptBuffer = new StringBuffer();
		private boolean done = false;
		
		public ScriptReader( Reader in )
		{
			super(in);
		}

		@Override
		public void close()
			throws IOException
		{
			// Do nothing.
		}

		@Override
		public int read(char[] cbuf, int off, int len)
			throws IOException
		{
			int count = 0;
			int c = read();
			if( c == -1 )
			{
				return(-1);
			}
			while( c != -1 && count < len )
			{
				cbuf[off + count] = (char)c;
				count++;
				c = read();
			}
			return(count);
		}
		
		public int read()
			throws IOException
		{
			if( done )
			{
				return( -1 );
			}
			int c;
			if( scriptBuffer.length() > 0 )
			{
				c = StringUtil.popFirstChar(scriptBuffer);
			}
			else
			{
				c = in.read();
			}
			if( c == '<' )
			{
				String closeTag = "</" + scriptFilterTag + ">";
				StringBuffer tagBuffer = new StringBuffer();
				tagBuffer.append((char)c);
				while( tagBuffer.length() < closeTag.length() &&
					closeTag.startsWith(tagBuffer.toString()) )
				{
					c = in.read();
					if( c == -1 )
					{
						scriptBuffer = tagBuffer;
						return( StringUtil.popFirstChar(scriptBuffer) );
					}
					tagBuffer.append((char)c);
					if( closeTag.equals(tagBuffer.toString()) )
					{
						done = true;
						return(-1);
					}
				}
				scriptBuffer = tagBuffer;
				return( StringUtil.popFirstChar(scriptBuffer) );
			}
			return(c);
		}
		
	}
	
	public ScriptSubFilter(Reader in)
	{
		super(in);
	}

	public int read()
		throws IOException
	{
		int ch;
		if( scriptException != null )
		{
			//throw( new IOException("Script Error", scriptException) );
			//To work with Java 5
			IOException e2 = new IOException("Some message"); 
			e2.initCause(scriptException);
			throw e2; 
		}
		if (readBuffer != null && readBuffer.length() > 0)
		{
			ch = StringUtil.popFirstChar(readBuffer);
		}
		else if( scriptOutput != null )
		{
			try
			{
				ch = scriptOutput.read();
				if( ch != -1 )
				{
					return(ch);
				}
			}
			catch( IOException e )
			{
				System.out.println( e.toString() );
			}
			scriptOutput = null;
			ch = in.read();
		}
		else
		{
			ch = in.read();
		}
		if (ch == -1)
		{
			return (-1);
		}
		char v = (char) ch;

		if (v != '<')
		{
			return (v);
		}
		int tagLength = scriptFilterTag.length();
		char[] testbuf = new char[tagLength];
		int read = in.read(testbuf, 0, tagLength);
		if (read == -1)
		{
			return (-1);
		}
		readBuffer = new StringBuffer("<");
		readBuffer = readBuffer.append(testbuf, 0, read);
		if (read < tagLength)
		{
			return (v);
		}
		/* We've found a matching tag for the script designation */
		if (readBuffer.toString().equals("<" + scriptFilterTag))
		{
			int t = in.read();
			if ((char) t != ' ' && (char) t != '>')
			{
				readBuffer.append((char) t);
				return (v);
			}
			while (t != -1 && t != '>')
			{
				readBuffer.append((char) t);
				t = in.read();
			}
			readBuffer.append((char) t);
			if (t == -1)
			{
				return (v);
			}
			/* we have a complete script tag */
			String scriptType = defaultScriptType;
			Matcher m = Pattern.compile("<" + scriptFilterTag + "(\\s+type\\s*=\\s*\"(\\w+)\"\\s*){0,1}>").matcher(readBuffer.toString());
			if (m.matches())
			{
				scriptType = m.group(2);
			}
			/* run in the script */
			readBuffer = null;
			scriptOutput = new PipedReader();
			(new ScriptExecutor(scriptType,in,scriptOutput)).start();
			return (read());
		}
		return (v);
	}

	public static void main(String[] args)
	{
		try
		{
			Reader base;
			if (args.length > 0)
			{
				base = new java.io.FileReader(args[0]);
			}
			else
			{
				base = new InputStreamReader(System.in);
			}

			ScriptSubFilter ssf = new ScriptSubFilter(base);
			for (int v = ssf.read(); v > -1; v = ssf.read())
			{
				System.out.print((char) v);
			}
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e.toString());
			e.printStackTrace(System.err);
		}
	}
}
