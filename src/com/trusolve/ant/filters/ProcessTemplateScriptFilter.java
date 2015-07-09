package com.trusolve.ant.filters;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptContext;

import org.apache.tools.ant.filters.BaseParamFilterReader;
import org.apache.tools.ant.types.Parameter;

import com.trusolve.io.DeferredReader;
import com.trusolve.io.ScriptFilter;
import com.trusolve.io.ScriptTemplateCodeGenerator;

public class ProcessTemplateScriptFilter
	extends BaseParamFilterReader
{
	private Reader originalReader = null;
	private DeferredReader deferredReader = null;
	private boolean initialized = false;
	private ScriptFilter sf = null;
	private ScriptTemplateCodeGenerator stcg = null;
	private String scriptType = "bsh";
	private String fileName = null;
	private String baseScriptTemplate = null;
	
	public ProcessTemplateScriptFilter(Reader in)
		throws IOException
	{
		this(new DeferredReader());
		originalReader = in;
	}

	private ProcessTemplateScriptFilter(DeferredReader in)
	{
		super(in);
		deferredReader = in;
	}
	
	public int read()
		throws IOException
	{
		if( ! initialized )
		{
			initialize();
		}
		return(super.read());
	}

	public void initialize()
		throws FileNotFoundException, IOException
	{
		Map<String,Object> attrs = new HashMap<String,Object>();
		attrs.put("project", getProject());
		attrs.put("originalReader", this.originalReader);
		for( Parameter p : this.getParameters() )
		{
			if( p.getName().equals( "baseScriptTemplate" ) )
			{
				this.baseScriptTemplate = p.getValue();
			}
			else if( p.getName().equals( "scriptType" ) )
			{
				this.scriptType = p.getValue();
			}
			else
			{
				attrs.put(p.getName(), p.getValue());
			}
		}
		stcg = new ScriptTemplateCodeGenerator( new FileReader( this.baseScriptTemplate ) );
		sf = new ScriptFilter( stcg );
		this.deferredReader.setReader( sf );
		sf.putAll(attrs);

		initialized=true;
	}
}
