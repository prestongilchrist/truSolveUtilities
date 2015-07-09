package com.trusolve.io;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;


/**
 * @author Preston Gilchrist
 * 
 * This class has been created to allow certain classes that require a reader in order to be
 * instantiated to still work and the actual reader to be associated later.
 *
 */
public class DeferredReader
	extends Reader
{
	private Reader source = null;

	@Override
	public int read(char[] cbuf, int off, int len)
		throws IOException
	{
		if( source == null )
		{
			throw( new IOException("Source reader has not been set.") );
		}
		return(source.read(cbuf, off, len));
	}

	@Override
	public void close()
		throws IOException
	{
		if( source == null )
		{
			throw( new IOException("Source reader has not been set.") );
		}
		source.close();
	}

	@Override
	public int read(CharBuffer target)
		throws IOException
	{
		if( source == null )
		{
			throw( new IOException("Source reader has not been set.") );
		}
		return source.read(target);
	}

	@Override
	public int read()
		throws IOException
	{
		if( source == null )
		{
			throw( new IOException("Source reader has not been set.") );
		}
		return source.read();
	}

	@Override
	public int read(char[] cbuf)
		throws IOException
	{
		if( source == null )
		{
			throw( new IOException("Source reader has not been set.") );
		}
		return source.read(cbuf);
	}

	@Override
	public long skip(long n)
		throws IOException
	{
		if( source == null )
		{
			throw( new IOException("Source reader has not been set.") );
		}
		return source.skip(n);
	}

	@Override
	public boolean ready()
		throws IOException
	{
		if( source == null )
		{
			throw( new IOException("Source reader has not been set.") );
		}
		return source.ready();
	}

	@Override
	public boolean markSupported()
	{
		if( source == null )
		{
			return(false);
		}
		return source.markSupported();
	}

	@Override
	public void mark(int readAheadLimit)
		throws IOException
	{
		if( source == null )
		{
			throw( new IOException("Source reader has not been set.") );
		}
		source.mark(readAheadLimit);
	}

	@Override
	public void reset()
		throws IOException
	{
		if( source == null )
		{
			throw( new IOException("Source reader has not been set.") );
		}
		source.reset();
	}
	
	public void setReader( Reader r )
		throws IOException
	{
		if( r != null )
		{
			this.source = r;
		}
		else
		{
			throw( new IOException("Attempted to change the reader for a deferred reader"));
		}
	}
}
