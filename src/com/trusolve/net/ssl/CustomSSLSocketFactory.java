package com.trusolve.net.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class CustomSSLSocketFactory
{

	public static SSLSocketFactory getCustomSSLContextFactory(InputStream trustStoreInputStream, String trustStorePassword, InputStream keyStoreInputStream, String keyStorePassword, String keyAlias)
		throws NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, CertificateException, IOException
	{
		SSLContext context = SSLContext.getInstance("SSLv3");
		context.init(createKeyManagers(keyStoreInputStream, keyStorePassword, keyAlias), createTrustManagers(trustStoreInputStream, trustStorePassword), null);

		return (context.getSocketFactory());
	}

	private static KeyManager[] createKeyManagers(InputStream keyStoreInputStream, String keyStorePassword, String keyAlias)
		throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException
	{
		// create keystore object, load it with keystorefile data
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(keyStoreInputStream, keyStorePassword == null ? null : keyStorePassword.toCharArray());
		// DEBUG information should be removed

		KeyManager[] managers;
		if (keyAlias != null)
		{
			managers = new KeyManager[] { new AliasKeyManager(keyStore, keyAlias, keyStorePassword) };
		}
		else
		{
			// create keymanager factory and load the keystore object in it
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, keyStorePassword == null ? null : keyStorePassword.toCharArray());
			managers = keyManagerFactory.getKeyManagers();
		}
		// return
		return managers;
	}

	private static TrustManager[] createTrustManagers(InputStream trustStoreInputStream, String trustStorePassword)
		throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
	{
		// create keystore object, load it with truststorefile data
		KeyStore trustStore = KeyStore.getInstance("JKS");
		trustStore.load(trustStoreInputStream, trustStorePassword == null ? null : trustStorePassword.toCharArray());
		// DEBUG information should be removed
		// create trustmanager factory and load the keystore object in it
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(trustStore);
		// return
		return trustManagerFactory.getTrustManagers();
	}
}
