package com.swisscom.cloud.sb.broker.util

import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.conn.ssl.TrustStrategy
import org.apache.http.conn.ssl.X509HostnameVerifier
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

import javax.net.ssl.SSLContext
import javax.net.ssl.SSLException
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocket
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

import static com.swisscom.cloud.sb.broker.util.RestTemplateDecorator.decorateWithBasicAuthentication

@Component
class RestTemplateFactory {
    RestTemplate build() { return new RestTemplate() }

    RestTemplate buildWithDigestAuthentication(String user, String password) {
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider(user, password)).useSystemProperties().build()
        return new RestTemplate(new HttpComponentsClientHttpRequestFactoryDigestAuth(client))
    }

    RestTemplate buildWithBasicAuthentication(String username, String password) {
        return decorateWithBasicAuthentication(new RestTemplate(), username, password)
    }

    RestTemplate buildWithProxy(String host, int port) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port)))
        return new RestTemplate(requestFactory)
    }

    RestTemplate buildWithSSLValidationDisabled() {

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(new SSLSocketFactory(new DummyTrustStrategy(), new DummyX509HostnameVerifier()))
                .build()

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory()
        requestFactory.setHttpClient(httpClient)
        return new RestTemplate(requestFactory)
    }

    RestTemplate buildWithSSLClientCertificate(String clientKeystorePath, String clientKeystorePassword) {
        def restTemplate = new RestTemplate()
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(HttpClients.custom().setSSLContext(getSSLContext(clientKeystorePath, clientKeystorePassword)).build()))
        restTemplate
    }

    private SSLContext getSSLContext(String clientKeystorePath,  String clientKeystorePassword) {
        SSLContext sslContext = SSLContexts.custom()
                .loadKeyMaterial(getKeyStore(clientKeystorePath, clientKeystorePassword), clientKeystorePassword.toCharArray())
                .loadTrustMaterial(new DummyTrustStrategy())
                .build()
        return sslContext
    }

    private KeyStore getKeyStore(String clientKeystorePath,  String clientKeystorePassword) {
        def keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(new FileInputStream(clientKeystorePath), clientKeystorePassword.toCharArray())
        return keyStore
    }

    static class DummyTrustStrategy implements TrustStrategy {
        @Override
        boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            return true
        }
    }

    static class DummyX509HostnameVerifier implements X509HostnameVerifier {
        @Override
        public void verify(String host, SSLSocket ssl) throws IOException {

        }

        @Override
        public void verify(String host, X509Certificate cert) throws SSLException {

        }

        @Override
        public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {

        }

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true
        }
    }

    private CredentialsProvider provider(String user, String password) {
        CredentialsProvider provider = new BasicCredentialsProvider()
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password)
        provider.setCredentials(AuthScope.ANY, credentials)
        return provider
    }

}
