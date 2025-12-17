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

import com.hitorro.util.core.Console;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.joining;

public class HTTPAsyncUtil {

    public static void a() {
        List<String> givenList = Arrays.asList("a", "bb", "ccc", "dd");

        String result = givenList.stream()
                .collect(joining(" "));

    }

    public static void foo() throws Exception {

        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(5))
                .build();

        final CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setIOReactorConfig(ioReactorConfig)
                .build();

        client.start();

        //final HttpHost target = new HttpHost("httpbin.org");
        //final String[] requestUris = new String[]{"/", "/ip", "/user-agent", "/headers"};

        final HttpHost target = new HttpHost("www.cnn.com", "https");
        final String[] requestUris = new String[]{"/2018/10/19/politics/lawmakers-skepticism-saudi-khashoggi/index.html",
                "/2018/10/19/politics/donald-trump-montana-speech/index.html",
                "/2018/10/19/media/reliable-sources-tim-dixon-podcast/index.html",
                "/2018/10/19/us/wisconsin-missing-girl-jayme-closs/index.html",
                "/2018/10/19/middleeast/turkey-khashoggi-intel-intl/index.html",
                "/2018/10/19/tech/new-tesla-model-3/index.html",
                "/2018/10/19/us/orionids-meteor-shower-october-trnd/index.html"};

        List<Future<SimpleHttpResponse>> results = new ArrayList<>();
        for (final String requestUri : requestUris) {
            final SimpleHttpRequest httpget = SimpleHttpRequest.create(Method.GET, target, requestUri);
            System.out.println("Executing request " + httpget.getMethod() + " " + httpget.getUri());
            final Future<SimpleHttpResponse> future = client.execute(
                    httpget,
                    new FutureCallback<SimpleHttpResponse>() {

                        @Override
                        public void completed(final SimpleHttpResponse response) {
                            System.out.println(requestUri + "->" + response.getCode());
                            System.out.println(response.getBody());
                        }

                        @Override
                        public void failed(final Exception ex) {
                            System.out.println(requestUri + "->" + ex);
                        }

                        @Override
                        public void cancelled() {
                            System.out.println(requestUri + " cancelled");
                        }

                    });
            results.add(future);
        }

        for (Future f : results) {
            Object o = f.get();
            Console.println();
        }
        System.out.println("Shutting down");
        client.close(CloseMode.GRACEFUL);
    }

    // Waits for all futures to complete and returns a list of results.
// If a future completes exceptionally then the resulting future will too.
    /*public static <T> CompletableFuture<List<T>> all(List<CompletableFuture<T>> futures) {
        CompletableFuture[] cfs = futures.toArray(new CompletableFuture[futures.size()]);

        return CompletableFuture.allOf(cfs)
                .thenApply(() -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
    }*/

}
