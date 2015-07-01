package com.trusolve.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.trusolve.io.ReplaceStringIOStream;
/*
 *
 *         Replaces all occurences of "replace" with the contents of "with"
 *         
 *         
 *      <filter>
 *              <filter-name>ReplaceStringFilter</filter-name>
 *              <filter-class>com.trusolve.servlet.ReplaceStringFilter</filter-class>
 *              <init-param>
 *                      <param-name>replace</param-name>
 *                      <param-value>xmlns:dsig="http://www.w3.org/2000/09/xmldsig#" xmlns="http://www.w3.org/2000/09/xmldsig#"</param-value>
 *              </init-param>
 *              <init-param>
 *                      <param-name>with</param-name>
 *                      <param-value>xmlns:dsig="http://www.w3.org/2000/09/xmldsig#"</param-value>
 *              </init-param>
 *      </filter>
 *      <filter-mapping>
 *              <filter-name>ReplaceStringFilter</filter-name>
 *              <url-pattern>/*</url-pattern>
 *      </filter-mapping>
 */
public class ReplaceStringFilter
	implements Filter
{
	private String replace = null;
	private String with = null;

	private static class ReqWrapper
		extends HttpServletRequestWrapper
	{
		private InputStream is;

		public ReqWrapper(HttpServletRequest request, String replace, String with)
			throws IOException
		{
			super(request);
			this.is = new ReplaceStringIOStream(super.getInputStream(),replace,with);
		}

		@Override
		public ServletInputStream getInputStream()
			throws IOException
		{
			return new ServletInputStreamWrapper(is);
		}

		@Override
		public BufferedReader getReader()
			throws IOException
		{
			String enc = getCharacterEncoding();
			if (enc == null) enc = "UTF-8";
			return new BufferedReader(new InputStreamReader(getInputStream(), enc));
		}
	}
	
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException
	{
		if (replace == null || with == null)
		{
			System.err.println("oops we are not configured");
			return;
		}
		if( request instanceof HttpServletRequest )
		{
			chain.doFilter(new ReqWrapper((HttpServletRequest)request, replace, with), response);
		}
		else
		{
			chain.doFilter(request, response);
		}
	}

	public void init(FilterConfig filterConfig)
		throws ServletException
	{
		this.replace = filterConfig.getInitParameter("replace");
		this.with = filterConfig.getInitParameter("with");
	}

	public void destroy()
	{
	}

}
