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

import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.iterator.sinks.BaseSink;
import org.apache.http.HttpException;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * apache httpclient 3.0 to 4.0 seems to be a pretty big shift.  Here is a tutorial on 4.0 features:
 * <p/>
 * http://hc.apache.org/httpcomponents-client-ga/tutorial/html/
 * <p/>
 * This client is pretty basic.  It supports posting data using an iterator and a sink target and equally an iterator is
 * returned that encapsulates the http response stream.
 * <p/>
 * The input object type and marshaling is handled by passing two mapping classes, one that provides outputstream->sink
 * and one for inputstream->iterator
 */
public class HTTPIterableClient<I, O> extends HTTPClientCore {
    private BaseMapper<InputStream, AbstractIterator<I>> inputStreamToIteratorMapper;

    private BaseMapper<OutputStream, BaseSink<O>> outputStreamToSinkMapper;

    private List<NameValuePair> nvpList = new ArrayList();

    public HTTPIterableClient(String username, String password,
                              BaseMapper<InputStream, AbstractIterator<I>> inputStreamToIteratorMapper,
                              BaseMapper<OutputStream, BaseSink<O>> outputStreamToSinkMapper) {
        super(username, password);
        this.inputStreamToIteratorMapper = inputStreamToIteratorMapper;
        this.outputStreamToSinkMapper = outputStreamToSinkMapper;
    }

    /**
     * You cannot include form parameters if your pushing up stream objects
     *
     * @param objectsToPushOut
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public AbstractIterator<I> postStream(String baseUrl, AbstractIterator<O> objectsToPushOut) throws IOException, HttpException {
        return postStream(baseUrl, objectsToPushOut, null);
    }

    public AbstractIterator<I> postStream(String baseUrl) throws IOException, HttpException {
        return postStream(baseUrl, null, nvpList);
    }

    public AbstractIterator<I> postStream(String url, AbstractIterator<O> objectsToPushOut, List<NameValuePair> nvList) throws IOException, HttpException {
        setupCreds(url);
        HttpPost postMethod = new HttpPost(url);

        setMethodHttpParams(postMethod);

        setupFormParameters(nvList, postMethod);

        setupOutputStream(objectsToPushOut, postMethod);

        return getReturnStream(url, postMethod);
    }

    public AbstractIterator<I> getStream(String url) throws IOException, HttpException {
        setupCreds(url);
        HttpGet postMethod = new HttpGet(url);

        setMethodHttpParams(postMethod);
        return getReturnStream(url, postMethod);
    }


    private AbstractIterator<I> getReturnStream(String url, final HttpRequestBase method) throws IOException, HttpException {
        InputStream is = getReturnInputStream(url, method);
        return inputStreamToIteratorMapper.apply(is);
    }

    private void setupFormParameters(final List<NameValuePair> nvList, final HttpPost postMethod) throws UnsupportedEncodingException {
        // need real params.
        if (!ListUtil.nullOrEmpty(nvList)) {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nvpList, "UTF-8");
            postMethod.setEntity(entity);
        }
    }

    private void setupOutputStream(final AbstractIterator<O> objectsToPushOut, final HttpPost postMethod) throws IOException {
        if (outputStreamToSinkMapper != null && objectsToPushOut != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BaseSink<O> sink = outputStreamToSinkMapper.apply(baos);
            objectsToPushOut.sink(sink);
            byte buff[] = baos.toByteArray();
            ByteArrayInputStream bios = new ByteArrayInputStream(buff);
            InputStreamEntity ise = new InputStreamEntity(bios, buff.length);
            postMethod.setEntity(ise);
        }
    }

    protected void setMethodHttpParams(final HttpRequestBase postMethod) {
        BasicHttpParams params = new BasicHttpParams();
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 0);

        postMethod.setParams(params);
    }
}
