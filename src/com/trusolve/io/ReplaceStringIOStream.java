package com.trusolve.io;

import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class ReplaceStringIOStream
	extends FilterInputStream
{
	private byte[] replace = null;
	private byte[] with = null;
	private byte[] readBuf = null;
	private int outBufPos = -1;
	private PushbackInputStream is = null;
	private int bytesRead = 0;
	
	public static void main( String[] args )
	{
		try
		{
			FileInputStream fis = new FileInputStream(args[0]);
			ReplaceStringIOStream rsios = new ReplaceStringIOStream(fis,args[1],args[2]);
			for(int i = rsios.read(); i >= 0 ; i = rsios.read())
			{
				System.out.write(i);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	protected ReplaceStringIOStream(InputStream in)
	{
		super(in);
		
	}
	public ReplaceStringIOStream(InputStream in, String replace, String with)
	{
		this(in);
		is = new PushbackInputStream(in, replace.length());
		this.replace = replace.getBytes();
		this.with = with.getBytes();
		this.readBuf = new byte[replace.length()];
	}
	@Override
	public int read()
		throws IOException
	{
		if( outBufPos > -1 )
		{
			int r = with[outBufPos];
			outBufPos++;
			if( outBufPos >= with.length )
			{
				outBufPos = -1;
			}
			bytesRead++;
			return(r);
		}
		int pos = -1;
		while( pos < ( replace.length - 1 ) &&
			( pos < 0 ||
			readBuf[pos] == replace[pos] ) )
		{
			pos++;
			int i = 0;
			try
			{
				i = is.read();
				readBuf[pos] = (byte)i;
				
				// We have reached EOF
				if( i < 0 )
				{
					if( pos == 0 )
					{
						return( i );
					}
					else
					{
						if( pos > 1 )
						{
							is.unread(readBuf, 1, pos - 1);
						}
						bytesRead++;
						return(readBuf[0]);
					}
				}
			}
			catch( IOException ioe )
			{
				if( pos == 0 )
				{
					throw( ioe );
				}
				else
				{
					if( pos > 1 )
					{
						is.unread(readBuf, 1, pos - 1);
					}
					bytesRead++;
					return(readBuf[0]);
				}
			}
		}
		// we have found our string
		if( pos == ( replace.length - 1 ) )
		{
			outBufPos = 1;
			bytesRead++;
			return(with[0]);
		}
		else if( pos > 0 )
		{
			is.unread(readBuf, 1, pos);
		}
		bytesRead++;
		return(readBuf[0]);
	}
	@Override
	public int read(byte[] b)
		throws IOException
	{
		return super.read(b);
	}
	@Override
	public int read(byte[] b, int off, int len)
		throws IOException
	{
		if( len == 0 )
		{
			return(0);
		}
		int i = 0;
		int c = read();
		if( c == -1 ) return( -1 );
		while( c != -1 && i < len )
		{
			b[i] = (byte)c;
			i++;
			if( i < len )
			{
				c = read();
			}
		}
		return(i);
	}
	@Override
	public void close()
		throws IOException
	{
		is.close();
		super.close();
	}
	@Override
	public synchronized void mark(int readlimit)
	{
		throw new RuntimeException(new IOException("mark/reset not supported"));
	}
	@Override
	public synchronized void reset()
		throws IOException
	{
		throw new IOException("mark/reset not supported");
	}
	@Override
	public boolean markSupported()
	{
		return false;
	}
	@Override
	public long skip(long n)
		throws IOException
	{
		int i = 0;
		while( i < n )
		{
			int r = read();
			if( r < 0 )
			{
				return(i);
			}
			i++;
		}
		return( i );
	}
	@Override
	public int available()
		throws IOException
	{
		return( is.available() + outBufPos + 1 );
	}

	public int getBytesRead()
	{
		return( this.bytesRead );
	}
}
