package com.trusolve.text;

import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.text.DateFormat;

public class ThreadsafeSimpleDateFormat extends DateFormat
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6430759443169668350L;
	
	private ThreadlocalSimpleDateFormat formatter;
	
	private class ThreadlocalSimpleDateFormat extends ThreadLocal<SimpleDateFormat>
	{
		private String format;
		private DateFormatSymbols formatSymbols = null;
		private Locale locale = null;
		
		public ThreadlocalSimpleDateFormat( String format )
		{
			this.format = format;
		}
		public ThreadlocalSimpleDateFormat( String format, DateFormatSymbols formatSymbols )
		{
			this.format = format;
			this.formatSymbols = formatSymbols;
		}
		public ThreadlocalSimpleDateFormat( String format, Locale locale )
		{
			this.format = format;
			this.locale = locale;
		}
		protected SimpleDateFormat initialValue()
		{
			if( formatSymbols != null )
			{
				return( new SimpleDateFormat(format, formatSymbols) );
			}
			if( locale != null )
			{
				return( new SimpleDateFormat(format, locale) );
			}
			return( new SimpleDateFormat(format) );
		}
	}
	
	public ThreadsafeSimpleDateFormat(String format)
	{
		formatter = new ThreadlocalSimpleDateFormat(format);
	}
	public ThreadsafeSimpleDateFormat(String format, DateFormatSymbols formatSymbols )
	{
		formatter = new ThreadlocalSimpleDateFormat(format,formatSymbols);
	}
	public ThreadsafeSimpleDateFormat(String format, Locale locale)
	{
		formatter = new ThreadlocalSimpleDateFormat(format, locale);
	}

	
	public Date parse( String string ) throws ParseException
	{
		return( formatter.get().parse( string ) );
	}

	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition)
	{
		return( formatter.get().format(date, toAppendTo, fieldPosition) );
	}

	@Override
	public Date parse(String source, ParsePosition pos) {
		return( formatter.get().parse(source, pos) );
	}
}
