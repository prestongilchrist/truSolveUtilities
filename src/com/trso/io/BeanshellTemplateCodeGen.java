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
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;

import com.trso.util.StringUtil;

/**
 * @author Preston Gilchrist
 *
 */
public class BeanshellTemplateCodeGen
	extends FilterReader
{
	@SuppressWarnings("unused")
	private static final String CLASS_ID = "$Id$";

	public enum BeanshellTemplateReadState
	{
		Output, // in normal output
		Comment, // 
		Expression, // <%= expression %>
		Scriptlet, // <% code fragment %>
	}
	
	private static boolean DEBUG = false;
	private boolean escaped = false;
	private boolean newline = false;
	private static final int PUSHBACKBUFFER = 3;
	private static final int MAXBSHLINESIZE = 2048;
	private static final String scriptOutputObject = "this.interpreter";
	private int readCharCount = 0;
	StringBuffer readBuffer;
	PushbackReader pin;
	BeanshellTemplateReadState readState = null;
	

	public BeanshellTemplateCodeGen( Reader in )
	{
		this( new PushbackReader( in, PUSHBACKBUFFER ) );
	}

	public BeanshellTemplateCodeGen( PushbackReader in )
	{
		super(in);
		pin = in;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)
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
		
			BeanshellTemplateCodeGen btcg = new BeanshellTemplateCodeGen(base);
			for( int v = btcg.read(); v > -1 ; v = btcg.read() )
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
	@Override
	public int read(char[] cbuf, int off, int len)
		throws IOException
	{
		if( DEBUG )
		{
			System.err.println( "Enter: read( buffer, " + off + ", " + len );
		}
		if( len == 0 )
		{
			return(0);
		}
		int i = 0;
		int c = read();
		if( c == -1 ) return( -1 );
		while( c != -1 && i < len )
		{
			cbuf[i] = (char)c;
			i++;
			if( i < len )
			{
				c = read();
			}
		}
		return(i);
	}

	@Override
	public int read()
		throws IOException
	{
		if( DEBUG )
		{
			System.err.println( "Enter: read()" );
		}
		// Wrap the whole function in a try/finally block so we can perform some
		// accounting after everything is done.
		try
		{
			/*
			 * If there are any contents in the readBuffer those are passed out
			 * without any additional parsing.
			 */
			if (readBuffer != null && readBuffer.length() > 0)
			{
				return (StringUtil.popFirstChar(readBuffer));
			}
			// Pull the next character off of the Reader
			int c = pin.read();

			// Handle EOF
			if (c == -1)
			{
				// If we were in the middle of an output operation, close the
				// current
				// print command and feed that out.
				if (readState == BeanshellTemplateReadState.Output)
				{
					closeOutputState(new StringBuffer());
					return (StringUtil.popFirstChar(readBuffer));
				}
				return (-1);
			}
			// Handles converting newline and carriage returns into escaped
			// characters for the prin
			if (readState == BeanshellTemplateReadState.Output && (c == '\n' || c == '\r'))
			{
				if (c == '\n')
				{
					readBuffer = new StringBuffer("n");
				}
				if (c == '\r')
				{
					readBuffer = new StringBuffer("r");
				}
				newline = true;
				return ('\\');
			}
			// Handle when we are inside of a BSH tag of some kind
			if (readState != BeanshellTemplateReadState.Output && readState != null)
			{
				// we are inside of a tag
				if (c == '%')
				{
					int next = in.read();
					if (next == -1) return (c);
					if (next == '>')
					{
						// close the tag operation
						switch (readState)
						{
							case Expression:
								readBuffer = new StringBuffer(");\n");
								readState = null;
								return (' ');
						}
						readState = null;
						return ('\n');
					}
				}
				return (c);
			}
			if (c == '<')
			{
				// this may be the start of a tag
				int next = pin.read();
				// If EOF is reach, then the < is part of the characters
				if (next != -1)
				{
					if (next == '%')
					{
						// We now know we are in a tag, pull the next char to see
						// what type of tag
						next = pin.read();
						// If we reach EOF in a tag, simply return EOF
						if (next == -1)
						{
							return (-1);
						}
						// It's starting to look like a BSH comment
						else if (next == '-')
						{
							next = pin.read();
							// If we reach EOF in a tag, simply return EOF
							if (next == -1) return(-1);
							if (next != '-')
							{
								// This is not a comment.  push the char's back on the Reader
								pin.unread(next);
								pin.unread("%-".toCharArray());
								return ('<');
							}
							// This is a comment.  Read all of the chars from the Reader
							// until the comment is over and then return the next char
							BeanshellTemplateReadState oldReadState = readState;
							while (readState != BeanshellTemplateReadState.Comment)
							{
								next = read();
								if (next == -1)
								{
									readState = null;
									return (-1);
								}
							}
							readState = oldReadState;
							return (next);
						}
						else if (next == '=')
						{
							// This is an Expression
							readBuffer = new StringBuffer();
							if (readState == BeanshellTemplateReadState.Output)
							{
								closeOutputState(readBuffer);
							}
							readState = BeanshellTemplateReadState.Expression;
							readBuffer.append(scriptOutputObject + ".print( ");
							return (StringUtil.popFirstChar(readBuffer));
						}
						else if (next == '%')
						{
							// if <% is intended to be in the text stream it is entered as
							// <%% which will end up as <% in the final filtered output
						}
						else
						{
							if (readState == BeanshellTemplateReadState.Output)
							{
								closeOutputState( new StringBuffer());
							}
							readState = BeanshellTemplateReadState.Scriptlet;							
							return (read());
						}
					}
					// This is not a tag, so push the next char back on the Reader
					pin.unread(next);
				}
			}
			// Handles breaking the print statements on line breaks for
			// readability
			// and breaking lines after they are getting too long
			if ((readState == BeanshellTemplateReadState.Output && newline) || readCharCount > MAXBSHLINESIZE)
			{
				closeOutputState(new StringBuffer());
				openOutputState(readBuffer);
				if( c == '<' )
				{
					readBuffer.append((char)c);
				}
				else
				{
					pin.unread(c);
				}
				return (StringUtil.popFirstChar(readBuffer));
			}
			if (readState == null)
			{
				// We have no read state which means we are at the begging of
				// the document
				// or just got out of another directive and
				// are not in a code snippet, therefore we must setup a print
				pin.unread(c);
				newline = false;
				readBuffer = new StringBuffer(scriptOutputObject + ".print( \"");
				readState = BeanshellTemplateReadState.Output;
				return (StringUtil.popFirstChar(readBuffer));
			}

			if (readState == BeanshellTemplateReadState.Output && (c == '"' || c == '\\'))
			{
				if (!escaped)
				{
					escaped = true;
					pin.unread(c);
					return ('\\');
				}
				escaped = false;
			}
			return (c);
		}
		finally
		{
			if (readState == BeanshellTemplateReadState.Output)
			{
				readCharCount++;
			}
		}
	}
	
	private void closeOutputState(StringBuffer sb)
	{
		readState = null;
		readBuffer.append( "\" );\n" );
	}
	private void openOutputState( StringBuffer sb )
	{
		readState = BeanshellTemplateReadState.Output;
		readCharCount = 0;
		newline = false;
		escaped = false;
		readBuffer.append( scriptOutputObject );
		readBuffer.append( ".print( \"" );
	}
}
