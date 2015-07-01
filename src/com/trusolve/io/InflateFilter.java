/**
 * This class implements a FilterReader that takes Beanshell code as input and outputs the
 * output results of executing the script.  The writer used by the script will be captured
 * in the ScriptContext and that data written to the writer by the executed script will be
 * the resulting data in the read operations of the script.
 */
package com.trusolve.io;

import java.util.zip.InflaterInputStream;

/**
 * @author Preston Gilchrist
 *
 */
public class InflateFilter
{
	@SuppressWarnings("unused")
	private static final String CLASS_ID = "$Id$";
	
	/**
	 * This a convenience method to allow command line execution of the Script filter against a file.
	 * 
	 * @param args a list of files which to process with the Script filter reader
	 */
	public static void main( String[] args )
	{
		try
		{
			InflaterInputStream iis = new InflaterInputStream(System.in);
			for( int i = iis.read(); i > -1 ; i = iis.read() )
			{
				System.out.write(i);
			}
		}
		catch(Exception e)
		{
			System.err.println("Error: " + e.toString());
			e.printStackTrace(System.err);
		}
	}
}
