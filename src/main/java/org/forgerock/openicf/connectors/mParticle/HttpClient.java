package org.forgerock.openicf.connectors.mParticle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.SecurityUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.json.JSONObject;


public class HttpClient {
    private static final Log logger = Log.getLog(HttpClient.class);

    private final org.apache.http.client.HttpClient client;
    private final mParticleConfiguration config;
    private final String authHeader;
    public static final BasicResponseHandler HANDLER_INSTANCE = new BasicResponseHandler();
    ConnectionKeepAliveStrategy conKeepAliveStrategy = new ConnectionKeepAliveStrategy() {
        @Override
        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            HeaderElementIterator it = new BasicHeaderElementIterator
                    (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && param.equalsIgnoreCase
                        ("timeout")) {
                    return Long.parseLong(value) * 1000;
                }
            }
            return 5 * 1000;
        }
    };

    /**
     * Constructor
     *
     * @param config the configuration to use
     */
    HttpClient(final mParticleConfiguration config) {
        this.config = config;
        authHeader = "Basic " + new String(Base64.encodeBase64((config.getServerKey() + ":" + SecurityUtil.decrypt(
                config.getServerSecret())).getBytes(StandardCharsets.ISO_8859_1)));
        this.client = HttpClients.custom().setKeepAliveStrategy(conKeepAliveStrategy).setConnectionManager(
                config.ConnectionManager()).build();
    }

    /**
     * Execute the request
     *
     * @param request request to execute
     * @return response in JSON format
     * @throws IOException when request execution fails
     */
    private String execute(final HttpRequestBase request) throws IOException {
        if (request == null) {
            logger.error("Request cannot be null");
            throw new ConnectorException("Request cannot be null");
        }

        request.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
        request.addHeader(HttpHeaders.CONNECTION, "keep-alive");
        request.addHeader("Cache-Control", "no-cache");
        try {
            return this.client.execute(request, HANDLER_INSTANCE);
        } catch (final Exception e) {
            logger.warn("API exception occurred. The response body is: {0}", e);
            throw e;
        } finally {
            request.releaseConnection();
        }

    }

    /**
     * Call GET request without params
     *
     * @param uri endpoint to call
     * @return response in JSON format
     * @throws IOException when request execution fails
     */
    String getRequest(final String uri) throws IOException {
        if (uri == null) {
            logger.error("URL cannot be null");
            throw new ConnectorException("URL cannot be null");
        }
        final HttpGet request = new HttpGet(uri);
        return this.execute(request);
    }

    /**
     * Call POST request with params
     *
     * @param uri  endpoint to call
     * @param data POST data
     * @return response in JSON format
     * @throws IOException when request execution fails
     */
    String postRequest(final String uri, final JSONObject data) throws IOException {
        if (uri == null) {
            logger.error("URL cannot be null");
            throw new ConnectorException("URL cannot be null");
        }
        if (data == null) {
            logger.error("POST data cannot be null");
            throw new NullPointerException("POST data cannot be null");
        }

        final HttpPost request = new HttpPost(uri);
        request.setEntity(new ByteArrayEntity(data.toString().getBytes(StandardCharsets.UTF_8)));
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Accept", "application/json");
        return this.execute(request);
    }


    /**
     * Call DELETE request with params
     *
     * @param uri endpoint to call
     * @throws IOException when request execution fails
     */
    void deleteRequest(final String uri) throws IOException {
        if (uri == null) {
            logger.error("URL cannot be null");
            throw new ConnectorException("URL cannot be null");
        }
        final HttpDelete request = new HttpDelete(uri);
        this.execute(request);
    }

    /**
     * Tests the connection to mParticle instance
     */
    public void testConnection() {
        try {
            postRequest(config.getApiEndpoint(), new JSONObject());
        } catch (final IOException e) {
            logger.info("Test connection failed: " + e.toString());
            throw new ConnectorException("Test connection failed", e);
        }
    }
}