package util;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.FactoryBean;

public class TrustSelfSignedCertHttpClientFactory implements FactoryBean<HttpClient> {

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public Class<?> getObjectType() {
		return HttpClient.class;
	}

	@Override
	public HttpClient getObject() throws Exception {

		// provide SSLContext that allows self-signed certificate
		SSLContext sslContext =
				new SSLContextBuilder()
		//.loadTrustMaterial(null, new TrustSelfSignedStrategy())
		.loadTrustMaterial(null, new TrustStrategy(){

			@Override
			public boolean isTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
				// TODO Auto-generated method stub
				return true;
			}

		})
		.build();

		;

		SSLConnectionSocketFactory sslConnectionSocketFactory
		= new SSLConnectionSocketFactory(sslContext, 
				new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		} );

		// based on HttpClients.createSystem()
		return HttpClientBuilder.create()
				.useSystemProperties()
				.setSSLSocketFactory(sslConnectionSocketFactory)  // add custom config
				.build();
	}
}
