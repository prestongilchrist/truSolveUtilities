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
import java.util.HashMap;
import java.util.Map;

import com.trso.util.StringUtil;

/**
 * @author Preston Gilchrist
 *
 */
public class ScriptTemplateCodeGenerator
	extends FilterReader
{
	@SuppressWarnings("unused")
	private static final String CLASS_ID = "$Id$";

	public enum ScriptTemplateReadState
	{
		Output, // in normal output
		Comment, // 
		Expression, // <%= expression %>
		Scriptlet, // <% code fragment %>
	}

	private static final int PUSHBACKBUFFER = 3;

	private boolean DEBUG = false;
	private boolean newline = false;
	private int maxScriptLineSize = 2048;
	private int readCharCount = 0;
	private StringBuffer readBuffer;
	private PushbackReader pin;
	private ScriptTemplateReadState readState = null;

	
	private String scriptType = "bsh";
	/**
	 * @return the scriptOutputPrintOpen
	 */
	public String getScriptType()
	{
		return scriptType;
	}

	/**
	 * @param scriptOutputPrintOpen the scriptOutputPrintOpen to set
	 */
	public void setScriptType( String scriptType )
	{
		this.scriptType = scriptType;
		if( scriptType.equals( "bsh" ) )
		{
			setScriptOutputPrintOpen("this.interpreter.print( ");
			setScriptOutputPrintStringQuotationOpen( "\"" );
			setScriptOutputPrintStringQuotationClose( "\"" );
			setScriptOutputPrintClose(" );\n");
			Map<Character,String> newEscapeChars = new HashMap<Character,String>();
			newEscapeChars.put( '\n', "\\n" );
			newEscapeChars.put( '\r', "\\r" );
			newEscapeChars.put( '"', "\\\"" );
			newEscapeChars.put( '\\', "\\\\" );
			setEscapeCharacters( newEscapeChars );
			Map<Character,String> newNewLineTrigger = new HashMap<Character,String>();
			newNewLineTrigger.put( '\n', "\\n" );
			newNewLineTrigger.put( '\r', "\\r" );
			setNewlineTriggerCharacters( newNewLineTrigger );
		}
		else if( scriptType.equals( "js" ) )
		{
			setScriptOutputPrintOpen("this.interpreter.print( ");
			setScriptOutputPrintStringQuotationOpen( "\"" );
			setScriptOutputPrintStringQuotationClose( "\"" );
			setScriptOutputPrintClose(" );\n");
			Map<Character,String> newEscapeChars = new HashMap<Character,String>();
			newEscapeChars.put( '\n', "\\n" );
			newEscapeChars.put( '\r', "\\r" );
			newEscapeChars.put( '"', "\\\"" );
			newEscapeChars.put( '\\', "\\\\" );
			setEscapeCharacters( newEscapeChars );
		}
	}
	
	private String scriptOutputPrintOpen = "this.interpreter.print( ";
	/**
	 * @return the scriptOutputPrintOpen
	 */
	public String getScriptOutputPrintOpen()
	{
		return scriptOutputPrintOpen;
	}

	/**
	 * @param scriptOutputPrintOpen the scriptOutputPrintOpen to set
	 */
	public void setScriptOutputPrintOpen( String scriptOutputPrintOpen )
	{
		this.scriptOutputPrintOpen = scriptOutputPrintOpen;
	}

	private String scriptOutputPrintStringQuotationOpen = "\"";
	/**
	 * @return the scriptOutputPrintStringQuotationOpen
	 */
	public String getScriptOutputPrintStringQuotationOpen()
	{
		return scriptOutputPrintStringQuotationOpen;
	}

	/**
	 * @param scriptOutputPrintStringQuotationOpen the scriptOutputPrintStringQuotationOpen to set
	 */
	public void setScriptOutputPrintStringQuotationOpen( String scriptOutputPrintStringQuotationOpen )
	{
		this.scriptOutputPrintStringQuotationOpen = scriptOutputPrintStringQuotationOpen;
	}

	private String scriptOutputPrintStringQuotationClose = "\"";
	/**
	 * @return the scriptOutputPrintStringQuotationClose
	 */
	public String getScriptOutputPrintStringQuotationClose()
	{
		return scriptOutputPrintStringQuotationClose;
	}

	/**
	 * @param scriptOutputPrintStringQuotationClose the scriptOutputPrintStringQuotationClose to set
	 */
	public void setScriptOutputPrintStringQuotationClose( String scriptOutputPrintStringQuotationClose )
	{
		this.scriptOutputPrintStringQuotationClose = scriptOutputPrintStringQuotationClose;
	}

	private String scriptOutputPrintClose = " );\n";
	/**
	 * @return the scriptOutputPrintClose
	 */
	public String getScriptOutputPrintClose()
	{
		return scriptOutputPrintClose;
	}

	/**
	 * @param scriptOutputPrintClose the scriptOutputPrintClose to set
	 */
	public void setScriptOutputPrintClose( String scriptOutputPrintClose )
	{
		this.scriptOutputPrintClose = scriptOutputPrintClose;
	}

	private Map<Character,String> escapeCharacters = new HashMap<Character,String>();
	/**
	 * @return the escapeCharacters
	 */
	public Map<Character, String> getEscapeCharacters()
	{
		return escapeCharacters;
	}

	/**
	 * @param escapeCharacters the escapeCharacters to set
	 */
	public void setEscapeCharacters( Map<Character, String> escapeCharacters )
	{
		this.escapeCharacters = escapeCharacters;
	}

	private Map<Character,String> newlineTriggerCharacters = new HashMap<Character,String>();
	/**
	 * @return the newlineTriggerCharacters
	 */
	public Map<Character, String> getNewlineTriggerCharacters()
	{
		return newlineTriggerCharacters;
	}

	/**
	 * @param newlineTriggerCharacters the newlineTriggerCharacters to set
	 */
	public void setNewlineTriggerCharacters( Map<Character, String> newlineTriggerCharacters )
	{
		this.newlineTriggerCharacters = newlineTriggerCharacters;
	}

	public ScriptTemplateCodeGenerator( Reader in )
	{
		this( new PushbackReader( in, PUSHBACKBUFFER ) );
	}

	public ScriptTemplateCodeGenerator( PushbackReader in )
	{
		super(in);
		pin = in;
		setScriptType( scriptType );
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
		
			ScriptTemplateCodeGenerator btcg = new ScriptTemplateCodeGenerator(base);
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
				if (readState == ScriptTemplateReadState.Output)
				{
					closeOutputState(new StringBuffer());
					return (StringUtil.popFirstChar(readBuffer));
				}
				return (-1);
			}
			// Certain characters should trigger a new print line for readability
			if (readState == ScriptTemplateReadState.Output && newlineTriggerCharacters.containsKey( Character.valueOf( (char)c ) ) )
			{
				newline = true;
			}
			// Handles converting specified characters into escaped
			// characters for the printing
			if (readState == ScriptTemplateReadState.Output && escapeCharacters.containsKey( Character.valueOf( (char)c ) ) )
			{
				readBuffer = new StringBuffer( escapeCharacters.get( Character.valueOf( (char)c ) ) );
				return (StringUtil.popFirstChar( readBuffer ) );
			}
			// Handle when we are inside of a Script tag of some kind
			if (readState == ScriptTemplateReadState.Scriptlet || 
				readState == ScriptTemplateReadState.Expression ||
				readState == ScriptTemplateReadState.Comment )
			{
				// we are inside of a tag
				if (c == '%')
				{
					int next = in.read();
					if (next == -1) return (c);
					switch(next)
					{
						// close the tag
						case '>':
							switch (readState)
							{
								case Expression:
									readBuffer = new StringBuffer(scriptOutputPrintClose);
									readState = null;
									return ( StringUtil.popFirstChar( readBuffer ) );
							}
							readState = null;
							return ('\n');
						case '%':
							return(next);
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
						// It's starting to look like a Script comment
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
							char[] b = new char[]{ 'x','x' };
							while ( ! ( b[0] == '%' && b[1] == '>' ) )
							{
								next = read();
								if (next == -1)
								{
									readState = null;
									return (-1);
								}
								b[0] = b[1];
								b[1] = (char)next;
							}
							return (read());
						}
						else if (next == '=')
						{
							// This is an Expression
							readBuffer = new StringBuffer();
							if (readState == ScriptTemplateReadState.Output)
							{
								closeOutputState(readBuffer);
							}
							readState = ScriptTemplateReadState.Expression;
							readBuffer.append(scriptOutputPrintOpen);
							return (StringUtil.popFirstChar(readBuffer));
						}
						else if (next == '%')
						{
							// if <% is intended to be in the text stream it is entered as
							// <%% which will end up as <% in the final filtered output
						}
						else
						{
							if (readState == ScriptTemplateReadState.Output)
							{
								closeOutputState( new StringBuffer());
							}
							readState = ScriptTemplateReadState.Scriptlet;							
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
			if ((readState == ScriptTemplateReadState.Output && newline) || readCharCount > maxScriptLineSize)
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
				readBuffer = new StringBuffer(scriptOutputPrintOpen + scriptOutputPrintStringQuotationOpen);
				readState = ScriptTemplateReadState.Output;
				return (StringUtil.popFirstChar(readBuffer));
			}
			return (c);
		}
		finally
		{
			if (readState == ScriptTemplateReadState.Output)
			{
				readCharCount++;
			}
		}
	}
	
	private void closeOutputState(StringBuffer sb)
	{
		readState = null;
		readBuffer.append( scriptOutputPrintStringQuotationClose );
		readBuffer.append( scriptOutputPrintClose );
	}
	private void openOutputState( StringBuffer sb )
	{
		readState = ScriptTemplateReadState.Output;
		readCharCount = 0;
		newline = false;
		readBuffer.append( scriptOutputPrintOpen );
		readBuffer.append( scriptOutputPrintStringQuotationOpen );
	}
}
