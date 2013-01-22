package com.domo.featurebuilder.controller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.cache.HeaderConstants;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpController {

    private static HttpController instance;
    private HttpClient client = new DefaultHttpClient();
    private CookieStore cookieStore = new BasicCookieStore();
    private HttpContext httpContext = new BasicHttpContext();
    public final String SCHEME = "https";
    private final String dostamales = "192.168.56.101";
    public final String HOST = dostamales;
    public final String CHARSET = "UTF-8";
    public static final String METHOD_GET = HeaderConstants.GET_METHOD;
    public static final String METHOD_PUT = HeaderConstants.PUT_METHOD;
    public static final String METHOD_POST = "POST";
    public static final String METHOD_DELETE = HeaderConstants.DELETE_METHOD;

    private HttpController() {
        setup();
    }

    public static HttpController getInstance() {
        if (instance == null)
            instance = new HttpController();
        return instance;
    }

    private void setup() {
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        try {
            client = trustEveryone();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(EndpointController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(EndpointController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * We have our client trust everyone so we can test this on dostamales. TODO: When you know how to do this correctly,
     * fix it.
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private HttpClient trustEveryone() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext ctx = SSLContext.getInstance("TLS");
        X509TrustManager tm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        ctx.init(null, new TrustManager[]{tm}, null);
        SSLSocketFactory ssf = new SSLSocketFactory(ctx);

        ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        ClientConnectionManager ccm = client.getConnectionManager();

        SchemeRegistry sr = ccm.getSchemeRegistry();
        sr.register(new Scheme("https", ssf, 443));

        return new DefaultHttpClient(ccm, client.getParams());

    }

    public HttpResponse executeGetOnClient(String path, String[]... params) throws Exception {
        return executeOnClient(METHOD_GET, path, params);
    }

    public HttpResponse executePutOnClient(String path, String[]... params) throws Exception {
        return executeOnClient(METHOD_PUT, path, params);
    }

    public HttpResponse executeDeleteOnClient(String path, String[]... params) throws Exception {
        return executeOnClient(METHOD_DELETE, path, params);
    }

    public HttpResponse executePostOnClient(String path, String[]... params) throws Exception {
        return executeOnClient(METHOD_POST, path, params);
    }

    public HttpResponse executeOnClient(final String method, String endpoint, String[]... params) throws IOException, URISyntaxException {
        HttpRequestBase request = new HttpRequestBase() {
            @Override
            public String getMethod() {
                return method;
            }
        };

        URI uri = buildURI(endpoint, params);
        request.setURI(uri);

        return executeRequestOnClientWithContext(request);
    }

    public URI buildURI(String path, String[]... params) throws URISyntaxException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(SCHEME).setHost(HOST).setPath(path);
        for (String[] param : params)
            builder.addParameter(param[0], param[1]);
        return builder.build();
    }

    public void checkStatusOk(HttpResponse response) throws Exception {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200)
            throw new Exception("Status code not OK (200). Status code: " + statusCode);
    }

    public void consumeResponse(HttpResponse response) throws Exception {
        HttpEntity entity = response.getEntity();
        try {
            EntityUtils.consume(entity);
        } catch (IOException ex) {
            throw new Exception("Failed consuming response", ex);
        }
    }

    /**
     * This is used to execute all requests with the same httpContext.
     *
     * @param request
     * @return
     * @throws IOException
     */
    public HttpResponse executeRequestOnClientWithContext(HttpRequestBase request) throws IOException {
        System.out.println("Executing " + request.getURI());
        HttpResponse response = client.execute(request, httpContext);
        System.out.println("Response code: " + response.getStatusLine().getStatusCode());
        return response;
    }
}
