package com.trusolve.ant.filters;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.util.Iterator;

import org.apache.tools.ant.filters.BaseParamFilterReader;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class PropertiesTemplateParser
	extends BaseParamFilterReader
{
	private static final String className = "com.trusolve.ant.filters.PropertiesTemplateParser";
	private boolean initialized = false;
	private PipedWriter propFileWriter = new PipedWriter();
	private Reader template;
	private Exception writerException = null;
	private PropertiesConfiguration baseTemplate;

	public static void main(String[] args)
	{
		try
		{
			Reader input;
			if( args.length > 0 )
			{
				input = new java.io.FileReader( args[0] );
			}
			else
			{
				input = new InputStreamReader( System.in );
			}
		
			PropertiesTemplateParser ptp = new PropertiesTemplateParser(input);
			for( int v = ptp.read(); v > -1 ; v = ptp.read() )
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

	public PropertiesTemplateParser( Reader in )
		throws IOException
	{
		super(new PipedReader());
		propFileWriter.connect((PipedReader)this.in);
		this.template = in;
	}
	
	public int read()
		throws IOException
	{
		if( ! initialized )
		{
			try
			{
				initialize();
			}
			catch( Exception e )
			{
				//throw( new IOException( "Initialization Exception", e ) );
				//To work with Java 5
				IOException e2 = new IOException("Some message"); 
				e2.initCause(e);
				throw e2; 
			}
		}
		int c = in.read();
		if( writerException != null )
		{
			//throw( new IOException( "Script Exception", writerException ) );
			//To work with Java 5
			IOException e2 = new IOException("Script Exception" ) ; 
			e2.initCause(writerException);
			throw e2;
		}
		return( c );
	}
	private void overlayConfig( PropertiesConfiguration source, PropertiesConfiguration overlay )
	{
		for( Iterator<?> j = overlay.getKeys() ; 
			j.hasNext() ;)
		{
			String key = j.next().toString();
			if( key.startsWith(className))
			{
				continue;
			}
			if( source.containsKey(key) )
			{
				source.setProperty(key, overlay.getProperty(key) );
			}
		}
	}

	private void initialize()
		throws ConfigurationException
	{
		PropertiesConfiguration pc = new PropertiesConfiguration();
		pc.load(template);
		baseTemplate = new PropertiesConfiguration( pc.getString( className + ".BASEPROPTEMPLATE") );
		baseTemplate.load(in);
		for( Iterator<?> i = pc.getKeys(className + ".OVERLAYS") ;
			i.hasNext() ; )
		{
			PropertiesConfiguration overlay = new PropertiesConfiguration( pc.getString( i.next().toString() ) );
			overlayConfig( baseTemplate, overlay );
		}
		overlayConfig( baseTemplate, pc );
		( 
			new Thread()
			{
				public void run()
				{
					try
					{
						baseTemplate.save(propFileWriter);
					}
					catch( Exception e )
					{
						writerException = e;
					}
					finally
					{
						try
						{
							propFileWriter.flush();
							propFileWriter.close();
						}
						catch( Exception e ){}
					}
				}
			}
		).start();
		initialized = true;
	}
}
