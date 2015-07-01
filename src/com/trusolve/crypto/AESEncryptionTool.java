package com.trusolve.crypto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;

public class AESEncryptionTool
{
	//private SecretKey mSecret = null;
	private static SecretKey pbeKey = null;
	private static PBEParameterSpec pbeParamSpec = null;
	
	/**
	 * @param args
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws UnsupportedEncodingException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws NoSuchPaddingException 
	 * @throws InvalidKeyException 
	 * @throws InvalidParameterSpecException 
	 */
	public static void main(String[] args) throws InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidParameterSpecException
	{
		
		if("decryptbase64".equalsIgnoreCase(args[0]))
		{
			try
			{
				System.out.println((new AESEncryptionTool(args[1],args[2]).decryptBase64(args[3])));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if("encryptbase64".equalsIgnoreCase(args[0]))
		{
			try
			{
				System.out.println((new AESEncryptionTool(args[1],args[2]).encryptBase64(args[3])));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public AESEncryptionTool(String password, String salt)
		throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		
		this( password, salt, 128 );
	}

	public AESEncryptionTool(String password, String salt, int keySize)
		throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		
		byte[] saltnew = new byte[] {103 , -73 , -94 , 12 , -73 , -33 , 1 , 109};
		pbeParamSpec = new PBEParameterSpec(saltnew, 65536);
		 PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
	     SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWithSHA1AndDESede");
	     pbeKey = factory.generateSecret(pbeKeySpec);
	
	}

	public String encryptBase64(String plainText)
		throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException
	{
		Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES/CBC/PKCS5Padding");
		try {
			cipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		byte[] ciphertext = cipher.doFinal(plainText.getBytes("UTF-8"));		
		return(	Base64.encodeBase64URLSafeString(ciphertext));
	}
	
	public String decryptBase64(String base64CipherText)
		throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException
	{
		
		String[] encryptionData = base64CipherText.split("\\|");
		Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
		return( new String(cipher.doFinal(Base64.decodeBase64(encryptionData[0])), "UTF-8") );
	}
}