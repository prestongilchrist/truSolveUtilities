package com.trusolve.json;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class JsonDereferencer
{
	private static final Log LOGGER = LogFactory.getLog(JsonDereferencer.class);
	private boolean dereferenceLocalRefs = false;
	
	private JsonNode rootNode;
	private URL rootContext;
	
	public static void main(String[] args)
	{
		try
		{
			URL fileURL = new File(args[0]).toURI().toURL();

			ObjectMapper om = new ObjectMapper();
			om.enable(SerializationFeature.INDENT_OUTPUT);
			ObjectWriter ow = om.writer().withDefaultPrettyPrinter();
			System.out.println(ow.writeValueAsString(new JsonDereferencer(fileURL).dereference()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	JsonDereferencer(JsonNode rootNode, URL rootContext)
	{
		this.rootNode = rootNode;
		this.rootContext = rootContext;
	}
	
	JsonDereferencer(URL u)
		throws JsonProcessingException, IOException
	{
		this(new ObjectMapper().readTree(u), u);
	}

	JsonDereferencer(File f)
		throws JsonProcessingException, IOException
	{
		this(f.toURI().toURL());
	}

	JsonDereferencer(Reader r, URL rootContext)
		throws JsonProcessingException, IOException
	{
		this(new ObjectMapper().readTree(r), rootContext);
	}
	JsonDereferencer(String s, URL rootContext)
		throws JsonProcessingException, IOException
	{
		this(new ObjectMapper().readTree(s), rootContext);
	}

	
	public static String dereferenceToString( URL u )
		throws JsonProcessingException, IOException, URISyntaxException
	{
		return new JsonDereferencer(u).dereferenceToString();
	}

	public static String dereferenceToString( Reader r )
		throws JsonProcessingException, IOException, URISyntaxException
	{
		return dereferenceToString( r, null );
	}
	
	public static String dereferenceToString( Reader r, URL context )
		throws JsonProcessingException, IOException, URISyntaxException
	{
		return new JsonDereferencer(r,context).dereferenceToString();
	}

	public static JsonNode dereference( URL u )
		throws JsonProcessingException, IOException, URISyntaxException
	{
		return new JsonDereferencer(u).dereference();
	}
	
	
	public String dereferenceToString()
		throws JsonProcessingException, IOException, URISyntaxException
	{
		ObjectMapper om = new ObjectMapper();

		om.enable(SerializationFeature.INDENT_OUTPUT);
		ObjectWriter ow = om.writer().withDefaultPrettyPrinter();

		return(ow.writeValueAsString(dereference()));
	}
		
	public JsonNode dereference()
		throws JsonProcessingException, IOException, URISyntaxException
	{
		return dereference(this.rootNode, this.rootContext);
	}
	
	private JsonNode dereference( JsonNode o, URL context )
		throws JsonProcessingException, IOException, URISyntaxException
	{
		if( o instanceof ObjectNode )
		{
			ObjectNode jo = (ObjectNode)o;

			// first iterate through each item and dereference it
			for( Iterator<Map.Entry<String,JsonNode>> i = jo.fields() ; i.hasNext() ; )
			{
				Map.Entry<String,JsonNode> e = i.next();
				JsonNode valueObject = e.getValue();
				System.out.println( e.getKey() + ":" + e.getValue() );
				JsonNode d = dereference(e.getValue(), context);
				if( valueObject != d )
				{
					jo.replace(e.getKey(), d );
				}
			}
			
			// then check is we have a reference here that we need to handle
			JsonNode ref = jo.get("$ref");
			if( ref != null )
			{
				List<String> refs;
				
				if( ref.isTextual() )
				{
					refs = new ArrayList<String>();
					refs.add(ref.asText());
				}
				else if( ref.isArray() )
				{
					refs = getStringList(ref);
					if( refs == null )
					{
						refs = new ArrayList<String>();
					}
				}
				else
				{
					return o;
				}
				for(String refHref : refs)
				{
					if( ! dereferenceLocalRefs && refHref != null && refHref.startsWith("#") )
					{
						continue;
					}
					URL loadLocation = new URL(context, ref.asText() );

					JsonNode refJson = new ObjectMapper().readTree(loadLocation);
					// Dereference the existing document
					refJson = dereference(refJson, loadLocation);
					URI loadLocationURI = loadLocation.toURI();
					String fragment = loadLocationURI.getFragment();
					if( fragment != null && fragment.length() > 0 )
					{
						refJson = refJson.at(fragment);
					}
					jo.remove("$ref");
					JsonNode refDeep = jo.remove("$refDeep");

					if( jo.size() == 0 || refJson.isValueNode() || refJson.isArray() )
					{
						return refJson;
					}
					if( refDeep != null )
					{
						merge(jo, (ObjectNode)refJson, true);
					}
					else
					{
						merge(jo, (ObjectNode)refJson, false);
					}
				}	
			}
		}
		else if( o instanceof ArrayNode )
		{
			ArrayNode ja = (ArrayNode) o;
			for( int i = 0 ; i < ja.size() ; i++ )
			{
				JsonNode t = ja.get(i); 
				JsonNode d = dereference(t, context);
				if( t != d )
				{
					ja.remove(i);
					ja.insert(i, d);
				}
			}
		}
		return(o);
	}
	
	private ObjectNode merge(ObjectNode target, ObjectNode source, boolean mergeDeep )
	{
		List<String> refIncludes = getStringList(target.remove("$refIncludes"));
		List<String> refExcludes = getStringList(target.remove("$refExcludes"));

		for( Iterator<Map.Entry<String,JsonNode>> i = source.fields() ; i.hasNext() ; )
		{
			Map.Entry<String,JsonNode> e = i.next();
			if( refIncludes != null && ! refIncludes.contains(e.getKey()) )
			{
				continue;
			}
			if( refExcludes != null && refExcludes.contains(e.getKey()))
			{
				continue;
			}
			if( target.has(e.getKey()) )
			{
				if( target.get(e.getKey()) instanceof ObjectNode && e.getValue() instanceof ObjectNode && mergeDeep )
				{
					merge((ObjectNode)target.get(e.getKey()), (ObjectNode)e.getValue(), mergeDeep);
				}
			}
			else
			{
				target.set(e.getKey(), e.getValue());
			}
		}
		return(target);
	}
	
	
	private List<String> getStringList(JsonNode j)
	{
		if( j == null )
		{
			return null;
		}
		if( ! j.isArray() )
		{
			LOGGER.error("Expected array value and instead got " + j.getClass().getName() );
			return null;
		}
		List<String> r = new ArrayList<String>();
		ArrayNode an = (ArrayNode)j;
		for( Iterator<JsonNode> i = an.elements() ; i.hasNext() ; )
		{
			JsonNode value = i.next();
			if( value.isTextual() )
			{
				r.add(value.asText());
			}
		}
		return r;
	}
}
