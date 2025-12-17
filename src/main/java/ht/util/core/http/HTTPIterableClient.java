package ht.util.core.http;

import ht.util.core.ListUtil;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.core.iterator.sinks.BaseSink;
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
