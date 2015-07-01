/**
 * $Id$
 */
package com.trusolve.io;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * @author preston.gilchrist@trusolve.com
 *
 */
public class ReplaceLineFilter
	extends FilterReader
{

	@Override
	public int read()
		throws IOException
	{
		return super.read();
	}

	@Override
	public int read(char[] cbuf, int off, int len)
		throws IOException
	{
		return super.read(cbuf, off, len);
	}

	/**
	 * @param in
	 */
	public ReplaceLineFilter(Reader in)
	{
		super(in);
	}

}
