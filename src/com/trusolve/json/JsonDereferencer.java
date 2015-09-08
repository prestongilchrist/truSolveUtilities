package com.trusolve.json;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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
	private Map<URL,JsonNode> dependencies = new HashMap<URL,JsonNode>();
	
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
		return dereference(o, context, null);
	}
	
	private JsonNode dereference( JsonNode o, URL context, JsonNode importLocalRefs )
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
				LOGGER.debug( "Processing node for dereference: " + e.getKey() + ":" + e.getValue() );
				JsonNode d = dereference(e.getValue(), context, importLocalRefs);
				if( valueObject != d )
				{
					jo.replace(e.getKey(), d );
				}
			}
			
			// then check is we have a reference here that we need to handle
			JsonNode ref = jo.get("$ref");
			JsonNode refIgnore = jo.remove("$refIgnore");
			LOGGER.debug("Checking for reference.  ref=" + ref + " / refIgnore=" + refIgnore );
			if( ref != null && refIgnore == null )
			{
				List<String> refs;
					
				// Get the traditional single reference
				if( ref.isTextual() )
				{
					LOGGER.debug("Reference is a single value of " + ref.asText());
					refs = new ArrayList<String>();
					refs.add(ref.asText());
				}
				// Get a customized array reference (requires a merge)
				else if( ref.isArray() )
				{
					refs = getStringList(ref);
					LOGGER.debug("Reference is an array " + refs );
					if( refs == null )
					{
						return(o);
					}
				}
				else
				{
					// If refs is not defined
					LOGGER.warn("Found a ref entry, but the value wasn't valid: " + o);
					return o;
				}
				
				// Begin Processing the references
				for(String refHref : refs)
				{
					LOGGER.debug("Processing reference for " + refHref);
					if( refHref != null && refHref.startsWith("#") )
					{
						LOGGER.debug("Reference is on the document currently being processed");
						if( importLocalRefs != null && refHref.length() > 2 )
						{
							LOGGER.debug("Local reference is being imported into the root document");
							addLocalReference( context, importLocalRefs, refHref.substring(1) );
							return o;
						}
						if( refs.size() == 1 && ! dereferenceLocalRefs )
						{
							// If there is more than 1 reference we must ALWAYS dereference with a merge operation
							LOGGER.debug("This is a single local reference and dereference locals is off, including as is");
							return o;
						}
					}
					// Remove the reference control variables from the resulting JSON document
					jo.remove("$ref");
					// remove the refDeep control variable.
					// Variable set: the system will merge all JSON objects and their descendants
					// Variable unset: the system will merge only the JSON name/value pair within the ref
					JsonNode refDeep = jo.remove("$refDeep");
					LOGGER.debug("refDeep: " + refDeep);
					
					URL loadLocation = new URL(context, ref.asText() );
					
					LOGGER.debug("Reference load location is=" + loadLocation);
					JsonNode refJson = getJsonFromCache(loadLocation);
					if( refJson == null )
					{
						LOGGER.debug("Reference root JSON document loaded from source.");
						refJson = new ObjectMapper().readTree(loadLocation);
						// Dereference the existing document
						refJson = dereference(refJson, loadLocation, refJson);
						this.dependencies.put(loadLocation, refJson);
					}
					else
					{
						LOGGER.debug("Reference root JSON document loaded from cache.");
					}
					
					String fragment = loadLocation.toURI().getFragment();
					
					if( fragment != null && fragment.length() > 0 )
					{
						LOGGER.debug("JSON Fragment pointer=" + fragment);
						refJson = refJson.at(fragment);
						LOGGER.trace("JSON Fragment=" + refJson);
					}
					

					if( jo.size() == 0 || refJson.isValueNode() || refJson.isArray() )
					{
						LOGGER.debug("Returning the resultant ref");
						return refJson;
					}
					if( refDeep != null )
					{
						LOGGER.debug("Merging the ref deep");
						merge(jo, (ObjectNode)refJson, true);
					}
					else
					{
						LOGGER.debug("Merging the ref shallow");
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
				JsonNode d = dereference(t, context, importLocalRefs);
				if( t != d )
				{
					LOGGER.debug("Replacing the ref in the array at position " + i );
					LOGGER.debug("Old value=" + t);
					LOGGER.debug("New value=" + d);
					
					ja.remove(i);
					ja.insert(i, d);
				}
			}
		}
		LOGGER.debug("Returning the final resultant object" );
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
				LOGGER.debug("Ignoring object \"" + e.getKey() + "\" since it is NOT in the refIncludes." );
				continue;
			}
			if( refExcludes != null && refExcludes.contains(e.getKey()))
			{
				LOGGER.debug("Ignoring object \"" + e.getKey() + "\" since it is in the refExcludes." );
				continue;
			}
			if( target.has(e.getKey()) )
			{
				if( target.get(e.getKey()) instanceof ObjectNode && e.getValue() instanceof ObjectNode && mergeDeep )
				{
					LOGGER.debug("Merging key " + e.getKey()  );
					merge((ObjectNode)target.get(e.getKey()), (ObjectNode)e.getValue(), mergeDeep);
				}
				else
				{
					LOGGER.debug("Ignoring key \"" + e.getKey() + "\" because it already exists and we are performing a shallow merge.");
				}
			}
			else
			{
				LOGGER.debug("Adding key " + e.getKey()  );
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
	private JsonNode getJsonFromCache(URL url)
	{
		if( url == null)
		{
			return null;
		}
		for( Map.Entry<URL, JsonNode> e : this.dependencies.entrySet() )
		{
			if( url.sameFile(e.getKey()))
			{
				return(e.getValue());
			}
		}
		return null;
	}
	
	private void addLocalReference( URL context,  JsonNode sourceDocument, String jsonPath )
	{
		String fragment = "#" + jsonPath;
		JsonNode refJson = sourceDocument.at(jsonPath);
		// only attempt to add to the core document if the pointer exists in the target document
		// the root node is an object and the node doesn't already exist in the target document
		if( refJson != null && this.rootNode.isObject() && this.rootNode.at(jsonPath).isMissingNode() )
		{
			ObjectNode addPoint = (ObjectNode)this.rootNode;
			
			StringBuffer jsonPathLocation = new StringBuffer();
			
			for( String nodeName : jsonPath.substring(1).split("/") )
			{
				jsonPathLocation.append("/");
				jsonPathLocation.append(nodeName);
				
				JsonNode current = sourceDocument.at(jsonPathLocation.toString());
				JsonNode target = addPoint.get(nodeName);
				
				if( current == null )
				{
					LOGGER.error("Unable to merge undefined reference " + context + fragment );
					break;
				}
				if( target != null )
				{
					if( target == current )
					{
						break;
					}
					if( target.getNodeType() == current.getNodeType() )
					{
						if( target.isObject() )
						{
							addPoint = (ObjectNode)target;
							continue;
						}
						else
						{
							LOGGER.error("Refusing to merge non object node types " + context + fragment );
							break;
						}
					}
					else
					{
						LOGGER.error("Refusing to merge different reference types at pointer " + context + fragment );
						break;
					}
				}
				if( current == refJson )
				{
					addPoint.set(nodeName, refJson);
				}
				else
				{
					if( current.isObject() )
					{
						addPoint = addPoint.putObject(nodeName);
					}
					else
					{
						LOGGER.warn("Reached a non object in JSON Point reference " + context + fragment );
						addPoint.set(nodeName, current);
						break;
					}
				}
			}
		}
	}
}
