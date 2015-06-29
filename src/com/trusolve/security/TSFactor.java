/**
 * Filename: TSFactor.java
 * Creation Date: 8:55:51 PM Jan 24, 2011
 *
 *
 */
package com.trusolve.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.apache.commons.codec.binary.Base64;

/**
 * @author pwg
 *
 */
public class TSFactor
{
	private enum CharacterMode
	{
		DIGIT,
		HEX,
		ASCII
	}
	private String hashAlgorithm = "SHA-1";
	private CharacterMode credentialMode = CharacterMode.DIGIT;
	private int credentialSize = 8;
	private int frequency = 60000; // the milisecond frequency
	private int window = 5; // the number of value differences winthin the window of validation error
	private byte[] seed;

	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		// TODO Auto-generated method stub

		TSFactor tsf = new TSFactor( "This is a test of this crap".getBytes() );
		tsf.credentialMode = CharacterMode.HEX;
		tsf.credentialSize = 20;
		try
		{
			for( String creds : tsf.generateCredentialList() )
			{
				System.out.println( creds );
			}
		}
		catch( Exception e )
		{
			
		}
	}
	
	public TSFactor( byte[] nonce)
	{
		this.seed = nonce;
	}

	public boolean validateCredential( String password )
	{
		try
		{
			return( generateCredentialList().contains( password ) );
		}
		catch( Exception e )
		{
			
		}
		return(false);
	}
	
	public List<String> generateCredentialList()
		throws NoSuchAlgorithmException	
	{
		List<String> r = new ArrayList<String>();
		Calendar now = new GregorianCalendar();
		now.set( Calendar.MILLISECOND, 0 );
		now.set( Calendar.SECOND, ( now.get( Calendar.SECOND ) / frequency ) * frequency );
		long end = now.getTimeInMillis() + ( frequency * window );
		for( long t = now.getTimeInMillis() - ( frequency * window ) ; t <= end ; t += frequency )
		{
			r.add( generateCredential( t ) );
		}
		return(r);
	}
	
	public String generateCredential( long timeStamp )
		throws NoSuchAlgorithmException
	{
		byte[] credHash = generateCredentialHash( timeStamp );
		StringBuffer sb = new StringBuffer();
		switch( credentialMode )
		{
			case DIGIT:
				for( byte b : credHash )
				{
					sb.append( String.format( "%03d", b ).substring( 1 ) );
				}
				return( sb.substring( 0, credentialSize ) );
			case HEX:
				for( byte b : credHash )
				{
					sb.append( Integer.toHexString( 0xff & b ) );
				}
				return( sb.substring( 0, credentialSize ) );
			case ASCII:
				return( Base64.encodeBase64String( credHash ).substring( 0, credentialSize ) );
		}
		return(null);
	}
	private byte[] generateCredentialHash( long timeStamp )
		throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance( hashAlgorithm );
		md.update( seed );
		md.update( Long.toString( timeStamp ).getBytes() );
		return(md.digest());
	}

}
