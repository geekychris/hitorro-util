/*
 * Copyright (c) 2006-2026 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Fluent HTTP client wrapper over HttpComponents 5.
 *
 * <pre>{@code
 * HttpResponse<String> r = Http.get("https://api.example/things")
 *     .header("Authorization", "Bearer " + token)
 *     .query("limit", "50")
 *     .timeout(Duration.ofSeconds(10))
 *     .asString();
 * MyDto dto = Http.post("https://api.example/things").bodyJson(payload).asJson(MyDto.class);
 * }</pre>
 *
 * <p>Each request builds and closes a single-shot client. For high-volume callers, share a
 * {@link CloseableHttpClient} yourself and use {@link #executeWith}.
 */
public final class Http {

    private static final ObjectMapper JSON = new ObjectMapper();

    public enum Method { GET, POST, PUT, DELETE, PATCH, HEAD }

    public static Request get(String url)    { return new Request(Method.GET, url); }
    public static Request post(String url)   { return new Request(Method.POST, url); }
    public static Request put(String url)    { return new Request(Method.PUT, url); }
    public static Request delete(String url) { return new Request(Method.DELETE, url); }
    public static Request patch(String url)  { return new Request(Method.PATCH, url); }
    public static Request head(String url)   { return new Request(Method.HEAD, url); }

    private Http() {}

    /** Immutable response holder. */
    public record HttpResponse<T>(int status, T body, Map<String, String> headers) {
        public boolean isSuccess() { return status >= 200 && status < 300; }
    }

    public static final class Request {
        private final Method method;
        private final String url;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private final List<NameValuePair> query = new ArrayList<>();
        private final List<NameValuePair> form = new ArrayList<>();
        private String body;
        private ContentType bodyContentType;
        private Duration timeout = Duration.ofSeconds(30);

        private Request(Method method, String url) {
            this.method = method;
            this.url = url;
        }

        public Request header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public Request headers(Map<String, String> h) {
            headers.putAll(h);
            return this;
        }

        public Request query(String name, String value) {
            query.add(new BasicNameValuePair(name, value));
            return this;
        }

        public Request form(String name, String value) {
            if (body != null) throw new IllegalStateException("cannot mix body and form fields");
            form.add(new BasicNameValuePair(name, value));
            return this;
        }

        public Request body(String raw, ContentType contentType) {
            if (!form.isEmpty()) throw new IllegalStateException("cannot mix body and form fields");
            this.body = raw;
            this.bodyContentType = contentType;
            return this;
        }

        public Request bodyJson(Object o) {
            try {
                return body(JSON.writeValueAsString(o), ContentType.APPLICATION_JSON);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        public Request timeout(Duration d) {
            this.timeout = d;
            return this;
        }

        public HttpResponse<String> asString() {
            try (CloseableHttpClient client = defaultClient(timeout)) {
                return executeWith(client, String.class);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        public <T> HttpResponse<T> asJson(Class<T> type) {
            try (CloseableHttpClient client = defaultClient(timeout)) {
                return executeWith(client, type);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        public <T> HttpResponse<T> executeWith(CloseableHttpClient client, Class<T> type) {
            URI uri;
            try {
                URIBuilder b = new URIBuilder(url);
                if (!query.isEmpty()) b.addParameters(query);
                uri = b.build();
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("invalid URL: " + url, e);
            }

            HttpUriRequestBase req = new HttpUriRequestBase(method.name(), uri);
            headers.forEach(req::addHeader);
            req.setConfig(RequestConfig.custom()
                    .setResponseTimeout(Timeout.ofMilliseconds(timeout.toMillis()))
                    .setConnectionRequestTimeout(Timeout.ofMilliseconds(timeout.toMillis()))
                    .build());

            HttpEntity entity = buildEntity();
            if (entity != null) req.setEntity(entity);

            try (CloseableHttpResponse resp = client.execute(req)) {
                int status = resp.getCode();
                Map<String, String> respHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                for (Header h : resp.getHeaders()) respHeaders.putIfAbsent(h.getName(), h.getValue());
                String raw = resp.getEntity() == null ? "" :
                        EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                Object bodyObj;
                if (type == String.class) {
                    bodyObj = raw;
                } else if (type == Void.class || raw.isEmpty()) {
                    bodyObj = null;
                } else {
                    bodyObj = JSON.readValue(raw, type);
                }
                @SuppressWarnings("unchecked")
                HttpResponse<T> out = new HttpResponse<>(status, (T) bodyObj, Collections.unmodifiableMap(respHeaders));
                return out;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (org.apache.hc.core5.http.ParseException e) {
                throw new RuntimeException("failed to parse response entity", e);
            }
        }

        private HttpEntity buildEntity() {
            if (body != null) return new StringEntity(body, bodyContentType);
            if (!form.isEmpty()) return new UrlEncodedFormEntity(form, StandardCharsets.UTF_8);
            return null;
        }
    }

    /** Default single-shot client. Callers who want pooling should build their own. */
    public static CloseableHttpClient defaultClient() {
        return HttpClients.createDefault();
    }

    /**
     * Default single-shot client with the given TCP connect timeout applied to the
     * connection manager. Response and connection-request timeouts are still applied
     * per-request via {@link RequestConfig}.
     */
    public static CloseableHttpClient defaultClient(Duration connectTimeout) {
        return HttpClients.custom()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setDefaultConnectionConfig(ConnectionConfig.custom()
                                .setConnectTimeout(Timeout.ofMilliseconds(connectTimeout.toMillis()))
                                .build())
                        .build())
                .build();
    }
}
