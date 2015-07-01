/**
 * 
 */
package com.trusolve.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * @author preston.gilchrist@trusolve.com
 *
 */
public class MultiReadHttpServletRequest
	extends HttpServletRequestWrapper
{
	private byte[] body;
	private static final int MAX_BUFFER_SIZE = 1024;
	private boolean mAllContentLoaded = false;
	
	public MultiReadHttpServletRequest(HttpServletRequest httpServletRequest)
		throws IOException
	{
		this(httpServletRequest,-1);
	}
	
	public MultiReadHttpServletRequest(HttpServletRequest httpServletRequest, long maxSize)
		throws IOException
	{
		super(httpServletRequest);
		// Read the request body and save it as a byte array
		InputStream is = super.getInputStream();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[MAX_BUFFER_SIZE];
		int n = 0;
		long count = 0;
		while ( ( maxSize < 0 || count < maxSize ) && -1 != (n = is.read(buffer)))
		{
			output.write(buffer, 0, n);
			count += n;
		}
		if( count <= maxSize )
		{
			mAllContentLoaded = true;
		}
		body = output.toByteArray();
	}

	public boolean isAllContentLoaded()
	{
		return(this.mAllContentLoaded);
	}
	
	
	@Override
	public ServletInputStream getInputStream()
		throws IOException
	{
		return new ServletInputStreamWrapper(new ByteArrayInputStream(body));
	}
	
	@Override
	public BufferedReader getReader()
		throws IOException
	{
		String enc = getCharacterEncoding();
		if(enc == null) enc = "UTF-8";
		return new BufferedReader(new InputStreamReader(getInputStream(), enc));
	} 
}
