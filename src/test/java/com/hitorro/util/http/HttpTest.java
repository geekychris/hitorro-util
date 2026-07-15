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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Http")
class HttpTest {

    private HttpServer server;
    private int port;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        port = server.getAddress().getPort();
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private String url(String path) {
        return "http://127.0.0.1:" + port + path;
    }

    private void handler(String path, Handler h) {
        server.createContext(path, ex -> {
            try {
                h.handle(ex);
            } catch (Throwable t) {
                byte[] err = ("server error: " + t).getBytes(StandardCharsets.UTF_8);
                ex.sendResponseHeaders(500, err.length);
                try (OutputStream os = ex.getResponseBody()) { os.write(err); }
            }
        });
    }

    private static void respond(HttpExchange ex, int status, String contentType, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", contentType);
        ex.sendResponseHeaders(status, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }

    @Test
    @DisplayName("GET returns body and status")
    void getReturnsBody() {
        handler("/hello", ex -> respond(ex, 200, "text/plain", "world"));
        Http.HttpResponse<String> r = Http.get(url("/hello")).asString();
        assertThat(r.status()).isEqualTo(200);
        assertThat(r.body()).isEqualTo("world");
        assertThat(r.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("headers are sent and query params are appended")
    void headersAndQuery() {
        AtomicReference<String> auth = new AtomicReference<>();
        AtomicReference<String> queryString = new AtomicReference<>();
        handler("/x", ex -> {
            auth.set(ex.getRequestHeaders().getFirst("Authorization"));
            queryString.set(ex.getRequestURI().getQuery());
            respond(ex, 200, "text/plain", "ok");
        });
        Http.get(url("/x"))
                .header("Authorization", "Bearer secret")
                .query("a", "1")
                .query("b", "hello world")
                .asString();
        assertThat(auth.get()).isEqualTo("Bearer secret");
        assertThat(queryString.get()).contains("a=1").contains("b=hello");
    }

    @Test
    @DisplayName("POST with JSON body deserialises response")
    void postJsonRoundTrip() {
        handler("/echo", ex -> {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            respond(ex, 200, "application/json", body); // echo
        });
        Payload out = new Payload("chris", 42);
        Http.HttpResponse<Payload> r = Http.post(url("/echo")).bodyJson(out).asJson(Payload.class);
        assertThat(r.status()).isEqualTo(200);
        assertThat(r.body().name).isEqualTo("chris");
        assertThat(r.body().n).isEqualTo(42);
    }

    @Test
    @DisplayName("form body is URL-encoded")
    void formBody() {
        AtomicReference<String> received = new AtomicReference<>();
        handler("/form", ex -> {
            received.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(ex, 200, "text/plain", "ok");
        });
        Http.post(url("/form")).form("a", "1").form("b", "hello world").asString();
        assertThat(received.get()).isEqualTo("a=1&b=hello+world");
    }

    @Test
    @DisplayName("non-2xx status is reported without throwing")
    void non2xx() {
        handler("/bad", ex -> respond(ex, 418, "text/plain", "no coffee"));
        Http.HttpResponse<String> r = Http.get(url("/bad")).asString();
        assertThat(r.status()).isEqualTo(418);
        assertThat(r.isSuccess()).isFalse();
        assertThat(r.body()).isEqualTo("no coffee");
    }

    @Test
    @DisplayName("response headers are exposed case-insensitively")
    void responseHeaders() {
        handler("/hh", ex -> {
            ex.getResponseHeaders().add("X-Custom", "val");
            respond(ex, 200, "text/plain", "ok");
        });
        Http.HttpResponse<String> r = Http.get(url("/hh")).asString();
        assertThat(r.headers().get("x-custom")).isEqualTo("val");
    }

    public static class Payload {
        public String name;
        public int n;
        public Payload() {}
        public Payload(String name, int n) { this.name = name; this.n = n; }
    }

    @FunctionalInterface
    interface Handler {
        void handle(HttpExchange ex) throws Exception;
    }
}
