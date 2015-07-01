/**
 * 
 */
package com.trusolve.commons.html;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;


/**
 * @author preston.gilchrist@trusolve.com
 *
 */
public class HiddenForm
{
	private final static Logger logger = Logger.getLogger(HiddenForm.class);
	private Map<String,String[]> mFormData = null;
	private String mFormName = "hiddenFormData";
	private String mAction = null;
	
	public HiddenForm()
	{
	}
	
	public HiddenForm( Map<String,String[]> formData, String action )
	{
		this.mFormData = new HashMap<String,String[]>(formData);
		this.mAction = action;
	}
	
	public String generateForm(boolean autoSubmit)
	{
		StringBuffer sb = new StringBuffer();
		sb.append( "<script type=\"text/javascript\">\n" );
		sb.append( "function submitHiddenForm()\n" );
		sb.append( "{\n" );
		sb.append( "	document.forms[\"0\"].submit()\n" );
		sb.append( "}\n" );
		if( autoSubmit )
		{
			sb.append( "window.onload=submitHiddenForm;\n" );
		}
		sb.append( "</script>\n" );
		sb.append("<form name=\"" );
		sb.append(mFormName);
		sb.append("\" action=\"");
		sb.append(mAction);
		sb.append("\" method=\"post\">\n");
		for( String param : this.mFormData.keySet() )
		{
			for( String value : this.mFormData.get(param) )
			{
				sb.append("<input type=\"hidden\" name=\"" );
				sb.append(StringEscapeUtils.escapeHtml(param));
				sb.append("\" value=\"");
				sb.append(StringEscapeUtils.escapeHtml(value));
				sb.append("\">\n");
			}
		}
		sb.append("</form>\n");
		return( sb.toString() );
	}
	
	public void sendPostRedirect(HttpServletResponse response)
		throws IOException
	{
		logger.debug("inside sendPostRedirect()");
		response.setContentType("text/html");
		Writer r = response.getWriter();
		r.write("<html>\n");
		r.write("<head><title>Auto Post Form</title></head>\n");
		r.write("<body>\n");
		r.write(generateForm(true));
		r.write("</body>\n");
		r.write("</html>\n");
	}

}
