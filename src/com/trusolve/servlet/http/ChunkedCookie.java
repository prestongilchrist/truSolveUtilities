/**
 * 
 */
package com.trusolve.servlet.http;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author preston.gilchrist@trusolve.com
 *
 */
public class ChunkedCookie
{
	private String mValue = null;
	private String mComment = null;
	private String mDomain = null;
	private String mName = null;
	private String mPath = null;
	private int mVersion = 0;
	private int mMaxAge = -1;
	private boolean mSecure = false;
	private int mMaxChunkSize = 2048;
	
	public ChunkedCookie( )
	{
	}

	public ChunkedCookie( String name, String value )
	{
		this.mName = name;
		this.mValue = value;
	}
	public static ChunkedCookie getChunkedCookie(HttpServletRequest request, String cookieName)
	{
		StringBuffer sb = new StringBuffer();
		Cookie[] cs = request.getCookies();
		int maxChunkSize = 0;
		boolean cookieFound = false;
		if( cs == null )
		{
			return(null);
		}
		for( Cookie c : cs )
		{
			if( c.getName().startsWith(cookieName) )
			{
				String[] cStuff = c.getName().split("_");
				if( cStuff.length == 2 )
				{
					cookieFound = true;
					int chunkStart = Integer.parseInt(cStuff[1]);
					int chunkSize = c.getValue().length();
					if( sb.length() < chunkStart+chunkSize )
					{
						sb.setLength(chunkStart+chunkSize);
					}
					sb.replace(chunkStart, chunkStart + chunkStart+chunkSize, c.getValue());
					if( chunkSize > maxChunkSize )
					{
						maxChunkSize = chunkSize;
					}
				}
			}
		}
		if( cookieFound )
		{
			ChunkedCookie cc = new ChunkedCookie();
			cc.mValue = sb.toString();
			cc.mMaxChunkSize = maxChunkSize;
			return(cc);
		}
		else
		{
			return(null);
		}
	}


	public Object clone()
	{
		ChunkedCookie cc = new ChunkedCookie();
		cc.mComment = this.mComment;
		cc.mDomain = this.mDomain;
		cc.mMaxAge = this.mMaxAge;
		cc.mMaxChunkSize = this.mMaxChunkSize;
		cc.mName = this.mName;
		cc.mValue = this.mValue;
		cc.mVersion = this.mVersion;
		cc.mSecure = this.mSecure;
		return(cc);
	}
	public String getComment()
	{
		return(this.mComment);
	}
	public String getDomain()
	{
		return(this.mDomain);
	}
	public int getMaxAge()
	{
		return(this.mMaxAge);
	}
	public String getName()
	{
		return(this.mName);
	}
	public String getPath()
	{
		return(this.mPath);
	}
	public boolean getSecure()
	{
		return(this.mSecure);
	}
	public String getValue()
	{
		return(this.mValue);
	}
	public int getVersion()
	{
		return(this.mVersion);
	}
	public void setComment(String purpose)
	{
		this.mComment = purpose;
	}
	public void setDomain(String pattern)
	{
		this.mDomain = pattern;
	}
	public void setMaxAge(int expiry)
	{
		this.mMaxAge = expiry;
	}
	public void setPath(String uri)
	{
		this.mPath = uri;
	}
	public void setSecure(boolean flag)
	{
		this.mSecure = flag;
	}
	public void setValue(String newValue)
	{
		this.mValue = newValue;
	}
	public void setVersion(int v)
	{
		this.mVersion = v;
	}


	public List<Cookie> getCookies()
	{
		List<Cookie> cookies = new ArrayList<Cookie>();
		for( int i = 0 ; i < mValue.length() ; i+=mMaxChunkSize )
		{
			Cookie cookie = new Cookie(mName + "_" + i, 
				this.mValue.substring(i, ( this.mValue.length()<i+mMaxChunkSize )?this.mValue.length():i+mMaxChunkSize) );
			cookie.setComment(this.mComment);
			if( this.mDomain != null && this.mDomain.length() > 0 )
			{
				cookie.setDomain(this.mDomain);
			}
			
			cookie.setSecure(this.mSecure);
			if( this.mPath != null && this.mPath.length() > 0 )
			{
				cookie.setPath(this.mPath);
			}
			cookie.setVersion(this.mVersion);
			cookies.add(cookie);
		}		
		return(cookies);
	}
	public void addCookie(HttpServletResponse resp)
	{
		for( Cookie cookie : getCookies() )
		{
			resp.addCookie(cookie);					
		}
	}

	public void deleteCookie(HttpServletRequest request, HttpServletResponse response)
	{
		deleteCookie(request, response, this.mName, this.mDomain, this.mPath);
	}

	public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name, String domain, String path)
	{
		Cookie[] cs = request.getCookies();
		if( cs == null )
		{
			return;
		}
		for( Cookie c : cs )
		{
			String[] cStuff = c.getName().split("_");
			if( cStuff.length == 2 && cStuff[0].equals(name) )
			{
				Cookie app = new Cookie(c.getName(),"remove");
				app.setMaxAge(0);
				if( path != null && path.length() > 0 )
				{
					app.setPath(path);
				}
				if( domain != null && domain.length() > 0 )
				{
					app.setDomain(domain);
				}
				response.addCookie(app);
			}
		}
	}
	
}
