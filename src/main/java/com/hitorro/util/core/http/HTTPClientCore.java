/*
 * Copyright (c) 2006-2025 Chris Collins
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
package com.hitorro.util.core.http;

import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

public class HTTPClientCore {
    protected HttpClient httpClient;
    protected HttpClientBuilder httpClientBuilder;
    protected AuthScope authScope;
    protected String username;
    protected String password;

    public HTTPClientCore(String username, String password) {
        this.username = username;
        this.password = password;
        httpClientBuilder = HttpClientBuilder.create();
    }

    public static AuthScope createAuthScope(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);
        return new AuthScope(url.getHost(), url.getPort());
    }

    protected static NameValuePair createNameValuePair(String name, Collection<String> items) {
        String v = StringUtil.mergeWithJoinToken(items, ",");
        return new BasicNameValuePair(name, v);
    }

    public HttpClientBuilder getClientBuilder() {
        return httpClientBuilder;
    }

    protected HttpClient getClient() {
        if (httpClient == null) {
            httpClient = httpClientBuilder.build();
        }
        return httpClient;
    }

    protected InputStream getReturnInputStream(String url, final HttpRequestBase method) throws IOException, HttpException {
        HttpResponse httpResponse = getClient().execute(method);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            String s = Fmt.S("Http error %s %s url: %s credential:%s|%s", statusCode, httpResponse.getStatusLine().getReasonPhrase(), url, username, password);
            throw new HttpException(s);
        }
        return httpResponse.getEntity().getContent();
    }

    protected void setupCreds(final String baseUrl) throws MalformedURLException {
        if (!StringUtil.nullOrEmptyString(password)) {
            authScope = createAuthScope(baseUrl);
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password));
            if (authScope != null && credentialsProvider != null) {
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }
    }
}
