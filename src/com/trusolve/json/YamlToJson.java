package com.trusolve.json;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YamlToJson
{
	public static void main( String[] args)
	{
		URL source = null;
		try
		{
			URI u = new URI(args[0]);
			if( u.isAbsolute() )
			{
				source = u.toURL();
			}
			else
			{
				source = new File(args[0]).toURI().toURL();
			}
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			JsonNode jn = mapper.readTree(source);
			System.out.println(jn.toString());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public String convertToString(Reader r)
		throws JsonProcessingException, IOException
	{
		JsonNode jn = convert(r);

		ObjectMapper om = new ObjectMapper();
		
		om.enable(SerializationFeature.INDENT_OUTPUT);
		ObjectWriter ow = om.writer().withDefaultPrettyPrinter();
		return ow.writeValueAsString(jn);
	}
	
	public JsonNode convert(Reader r)
		throws JsonProcessingException, IOException
	{
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		JsonNode jn = mapper.readTree(r);
		return(jn);
	}
}
