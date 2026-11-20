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
package com.hitorro.util;

import tools.jackson.databind.*; import tools.jackson.databind.node.*;
import com.hitorro.util.persist.*; import com.hitorro.util.http.Http;
import com.hitorro.util.commandandcontrol.*; import com.hitorro.util.commandandcontrol.json.*;
import com.hitorro.util.zookeeper.JsonPayload;
import java.io.*; import java.net.*; import java.nio.file.*; import java.time.*; import java.util.*;
/**
 * Pins what hitorro-util writes and reads to what 3.1.0 did on Jackson 2.18: the
 * JsonFileEntityStore file (pretty printed, ISO java.time), reading files 3.x wrote (and the
 * IOException for bad ones), ClasspathManifestRegistry manifests, Http request bodies and
 * responses, the command-and-control JSONResponse stream, and the ZooKeeper JsonPayload.
 * {@code jackson2-util.txt} is the output of the same code run on hitorro-util 3.1.0 sources
 * (generator: ~/hitorro-j3/tools/Jackson2UtilGolden.java.txt in the migration workspace).
 * {@code jackson3-util-javatime.txt} holds the one deliberate difference: Http's mapper had no
 * JavaTimeModule in 3.x, so java.time values failed; Jackson 3 has java.time built in and Http
 * now writes them as Jackson 2 + JavaTimeModule would (timestamps).
 * Jackson 3 node and exception names are mapped back before comparing (StringNode = TextNode).
 */
class Jackson2FormatGoldenTest {
  static final Map<String, String> out = new LinkedHashMap<>();
  static void p(String k, Object v){ out.put(k, String.valueOf(v).replace("\\","\\\\").replace("\n","\\n").replace("\r","\\r")); }
  // Exceptions are described by what callers can see: checked IOException or not, and Jackson's own
  // exceptions as "jackson" (their classes differ between 2 and 3). A plain IOException that only
  // carries a Jackson exception counts as that exception thrown as an IOException.
  static boolean jackson(Throwable t){ return t.getClass().getName().startsWith("com.fasterxml.jackson") || t.getClass().getName().startsWith("tools.jackson"); }
  static String desc(Throwable t, boolean top){ boolean ioe = t instanceof IOException;
    if (t.getClass() == IOException.class && t.getCause() != null && jackson(t.getCause())) t = t.getCause();
    String s = (top ? (ioe ? "IOE:" : "RTE:") : "") + (jackson(t) ? "jackson" : t.getClass().getSimpleName());
    if (!jackson(t) && t.getCause() != null) s += "<" + desc(t.getCause(), false) + ">"; return s; }
  static String tryIt(java.util.concurrent.Callable<Object> c){ try { return String.valueOf(c.call()); } catch(Throwable t){ return "!"+desc(t, true); } }
  static String types(JsonNode n){ StringBuilder t=new StringBuilder(); if (n == null || !n.isObject()) return ""; Iterator<String> it=n.propertyNames().iterator(); while(it.hasNext()){String k=it.next(); t.append(k+":"+n.get(k).getClass().getSimpleName()+",");} return t.toString(); }
  public static class Item {
    public String id; public int n; public long big; public double d; public boolean flag; public Integer boxed;
    public Instant at; public Date date; public LocalDate day; public LocalDateTime ldt; public Duration dur;
    public List<String> tags; public Map<String, Object> meta; public String nul; public Item child;
    private String secret; public String getSecret(){ return secret; } public void setSecret(String s){ secret = s; }
    public String toString(){ return "Item[id="+id+",n="+n+",big="+big+",d="+d+",flag="+flag+",boxed="+boxed+",at="+at+",date="+(date==null?null:date.getTime())+",day="+day+",ldt="+ldt+",dur="+dur+",tags="+tags+",meta="+(meta==null?null:new TreeMap<>(meta))+",nul="+nul+",secret="+secret+",child="+child+"]"; }
  }
  public static class HItem {
    public String id; public int n; public long big; public double d; public float f; public boolean flag; public Integer boxed; public byte[] bytes;
    public List<String> tags; public Map<String, Object> meta; public String nul; public HItem child;
    private String secret; public String getSecret(){ return secret; } public void setSecret(String s){ secret = s; }
    public String toString(){ return "HItem[id="+id+",n="+n+",big="+big+",d="+d+",f="+f+",flag="+flag+",boxed="+boxed+",bytes="+(bytes==null?null:Arrays.toString(bytes))+",tags="+tags+",meta="+(meta==null?null:new TreeMap<>(meta))+",nul="+nul+",secret="+secret+",child="+child+"]"; }
  }
  static HItem hitem(String id){ HItem i = new HItem(); i.id = id; i.n = 7; i.big = 12345678901L; i.d = 1.10; i.f = 0.1f; i.flag = true; i.bytes = new byte[]{1,2,(byte)255};
    i.tags = Arrays.asList("x", "y/z", "\u00e9\u2028"); i.meta = new LinkedHashMap<>(); i.meta.put("z", 1); i.meta.put("a", Arrays.asList(1, "b", 2.5)); i.meta.put("m", new LinkedHashMap<>());
    i.setSecret("s3"); return i; }
  public static class Entry { public String id; public String title; public String bodyRes; public String body;
    public String toString(){ return "Entry[id="+id+",title="+title+",bodyRes="+bodyRes+",body="+body+"]"; } }
  static Item item(String id){ Item i = new Item(); i.id = id; i.n = 7; i.big = 12345678901L; i.d = 1.10; i.flag = true;
    i.at = Instant.parse("2026-11-20T10:15:30.123Z"); i.date = new Date(1763633730123L); i.day = LocalDate.of(2026,11,20);
    i.ldt = LocalDateTime.of(2026,11,20,10,15,30,500000000); i.dur = Duration.ofSeconds(90, 5000);
    i.tags = Arrays.asList("x", "y/z", "\u00e9\u2028"); i.meta = new LinkedHashMap<>(); i.meta.put("z", 1); i.meta.put("a", Arrays.asList(1, "b")); i.meta.put("m", new LinkedHashMap<>());
    i.setSecret("s3"); return i; }
  public static Map<String, String> compute(Path dir) throws Exception {
    out.clear();
    ObjectMapper om = com.hitorro.util.json.JsonMappers.json();
    // ---- JsonFileEntityStore: file format written, and reading files 3.x wrote
    { Path f = dir.resolve("store/items.json");
      JsonFileEntityStore<Item> st = new JsonFileEntityStore<>(f, Item.class, x -> x.id);
      Item b = item("b"); Item a = item("a"); a.child = item("c"); a.n = 0; a.d = -0.0; a.tags = new ArrayList<>(); a.at = Instant.ofEpochSecond(0);
      st.put(b); st.put(a);
      p("store.file.2", new String(Files.readAllBytes(f), "UTF-8"));
      st.update("b", x -> x.boxed = 42); st.remove("zzz");
      p("store.file.updated", new String(Files.readAllBytes(f), "UTF-8"));
      p("store.reload", tryIt(() -> new JsonFileEntityStore<>(f, Item.class, x -> x.id).all()));
      st.remove("a"); st.remove("b");
      p("store.file.empty", new String(Files.readAllBytes(f), "UTF-8"));
      String[][] files = {
        {"ok", "[{\"id\":\"q\",\"n\":3,\"at\":\"2026-01-02T03:04:05Z\",\"date\":\"2026-01-02T03:04:05.000+00:00\",\"day\":\"2026-01-02\",\"dur\":2.5,\"secret\":\"k\"}]"},
        {"numericDates", "[{\"id\":\"q\",\"at\":1700000000.5,\"date\":1700000000123,\"day\":[2026,1,2],\"ldt\":[2026,1,2,3,4,5]}]"},
        {"unknownProp", "[{\"id\":\"q\",\"what\":1}]"},
        {"nullPrimitive", "[{\"id\":\"q\",\"n\":null,\"flag\":null}]"},
        {"stringNumber", "[{\"id\":\"q\",\"n\":\"12\",\"d\":\"2.5\",\"big\":12.7}]"},
        {"trailing", "[{\"id\":\"q\"}] [1]"},
        {"malformed", "[{\"id\":"}, {"emptyFile", ""}, {"notArray", "{\"id\":\"q\"}"}, {"singleQuoted", "[{'id':'q'}]"},
        {"noId", "[{\"n\":1},{\"id\":\"\"},{\"id\":\"r\"}]"}, {"dupId", "[{\"id\":\"r\",\"n\":1},{\"id\":\"r\",\"n\":2}]"}};
      for (String[] in : files) {
        Path g = dir.resolve("in-" + in[0] + ".json"); Files.write(g, in[1].getBytes("UTF-8"));
        p("store.read." + in[0], tryIt(() -> new JsonFileEntityStore<>(g, Item.class, x -> x.id).all()));
      }
    }
    // ---- ClasspathManifestRegistry
    { String[][] mans = {
        {"ok", "[{\"id\":\"one\",\"title\":\"T\",\"bodyRes\":\"one.txt\"},{\"id\":\"two\"},{\"title\":\"noid\"},{\"id\":\"three\",\"bodyRes\":\"missing.txt\"}]"},
        {"unknownProp", "[{\"id\":\"one\",\"extra\":1}]"}, {"malformed", "[{"}, {"trailing", "[{\"id\":\"one\"}] x"}, {"object", "{\"id\":\"one\"}"}};
      for (String[] m : mans) {
        Path d = dir.resolve("cp-" + m[0]); Files.createDirectories(d.resolve("tpl"));
        Files.write(d.resolve("tpl/manifest.json"), m[1].getBytes("UTF-8")); Files.write(d.resolve("tpl/one.txt"), "body one\n".getBytes("UTF-8"));
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        PrintStream oldErr = System.err; ByteArrayOutputStream err = new ByteArrayOutputStream();
        try (URLClassLoader cl = new URLClassLoader(new URL[]{d.toUri().toURL()}, null)) {
          Thread.currentThread().setContextClassLoader(cl); System.setErr(new PrintStream(err, true, "UTF-8"));
          p("manifest." + m[0], tryIt(() -> new ClasspathManifestRegistry<>("tpl/manifest.json", "tpl", Entry.class, e -> e.id, e -> e.bodyRes, (e, b) -> e.body = b).all()));
        } finally { Thread.currentThread().setContextClassLoader(old); System.setErr(oldErr); }
        p("manifest." + m[0] + ".stderrLines", err.toString("UTF-8").split("\n").length + ":" + err.toString("UTF-8").replaceAll("(?s)load failed: .*", "load failed: <msg>"));
      }
    }
    // ---- Http: JSON sent and received
    { com.sun.net.httpserver.HttpServer srv = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      Map<String, String> bodies = new LinkedHashMap<>();
      bodies.put("/item", "{\"id\":\"h\",\"n\":5,\"extra\":{\"x\":1},\"at\":\"2026-01-02T03:04:05Z\",\"date\":1700000000123,\"tags\":[\"a\"]}");
      bodies.put("/nulls", "{\"id\":null,\"n\":null,\"boxed\":null}"); bodies.put("/bad", "{\"id\":"); bodies.put("/trailing", "{\"id\":\"t\"} {}");
      bodies.put("/hitem", "{\"id\":\"h\",\"n\":5,\"f\":1.25,\"bytes\":\"AQL/\",\"extra\":{\"x\":1},\"child\":{\"id\":\"c\",\"secret\":\"q\"},\"tags\":[\"a\",null],\"meta\":{\"k\":[1,1.5,12345678901,true]}}");
      bodies.put("/array", "[1,2]"); bodies.put("/empty", ""); bodies.put("/wrongType", "{\"n\":\"abc\"}");
      srv.createContext("/", ex -> { byte[] resp; String path = ex.getRequestURI().getPath();
        if (path.equals("/echo")) resp = ex.getRequestBody().readAllBytes(); else resp = bodies.getOrDefault(path, "").getBytes("UTF-8");
        ex.getResponseHeaders().add("Content-Type", "application/json"); ex.sendResponseHeaders(200, resp.length == 0 ? -1 : resp.length);
        if (resp.length > 0) ex.getResponseBody().write(resp); ex.close(); });
      srv.start();
      try {
        String base = "http://127.0.0.1:" + srv.getAddress().getPort();
        HItem h = hitem("a"); h.child = hitem("c"); h.child.bytes = null;
        p("http.bodyJson.hitem", tryIt(() -> Http.post(base + "/echo").bodyJson(h).asString().body()));
        p("http.bodyJson.list", tryIt(() -> Http.post(base + "/echo").bodyJson(Arrays.asList(hitem("l"), null, "s", 1L, 2.0)).asString().body()));
        // java.time: Http's 3.x mapper had no JavaTimeModule, so these failed (see the test).
        Item a = item("a"); a.child = item("c");
        p("http.javatime.bodyJson.item", tryIt(() -> Http.post(base + "/echo").bodyJson(a).asString().body()));
        Map<String, Object> m = new LinkedHashMap<>(); m.put("z", null); m.put("a", 1.5f); m.put("when", Instant.ofEpochMilli(5)); m.put("n", om.readTree("{\"k\":[1,\"2\"]}"));
        p("http.javatime.bodyJson.map", tryIt(() -> Http.post(base + "/echo").bodyJson(m).asString().body()));
        p("http.bodyJson.emptyBean", tryIt(() -> Http.post(base + "/echo").bodyJson(new Object()).asString().body()));
        p("http.bodyJson.record", tryIt(() -> Http.post(base + "/echo").bodyJson(new java.util.AbstractMap.SimpleEntry<>("k", "v")).asString().body()));
        for (String path : bodies.keySet()) {
          p("http.asJson.hitem" + path, tryIt(() -> Http.get(base + path).asJson(HItem.class).body()));
          p((path.equals("/item") ? "http.javatime" : "http") + ".asJson.item" + path, tryIt(() -> Http.get(base + path).asJson(Item.class).body()));
          p("http.asJson.node" + path, tryIt(() -> { JsonNode n = Http.get(base + path).asJson(JsonNode.class).body(); return n == null ? "null" : n.getClass().getSimpleName() + " " + n + " " + types(n); }));
          p("http.asJson.map" + path, tryIt(() -> Http.get(base + path).asJson(Map.class).body()));
        }
      } finally { srv.stop(0); }
    }
    // ---- JSONResponse: what command-and-control sends
    { ByteArrayOutputStream bos = new ByteArrayOutputStream();
      JSONResponse r = new JSONResponse("trans", bos);
      ResponseShape shape = new ResponseShape("trans", "row");
      shape.addHeader("Name", "Count", "Ratio", "Big"); shape.addHeaderShortNames("name", "count", "ratio", "big");
      shape.addRowTypes(String.class, Integer.class, Double.class, Long.class);
      r.setResponseShape(shape);
      r.addBannerRow("banner \u00e9 / \u2028 \"q\"");
      r.addRow("a/b", 5, 2.50, 12345678901L);
      r.addRow(null, "", 1e30, -3L);
      r.addRow("x\ty", 0, -0.0, 0L);
      r.addInfo("info"); r.addInfo(InfoLevel.Warn, "warn"); r.addInfo(InfoLevel.Error, "err </script>");
      r.end();
      p("jsonResponse", bos.toString("UTF-8"));
    }
    // ---- zookeeper JsonPayload
    { JsonPayload jp = new JsonPayload(); jp.setJsonNode(om.readTree("{\"b\":1.10,\"a\":[1,{\"x\":null}],\"s\":\"\u00e9/\",\"big\":12345678901234567890}"));
      p("jsonPayload.message", jp.message);
      p("jsonPayload.node", tryIt(() -> { JsonNode n = jp.getAsJsonNode(); return n + " " + types(n); }));
      jp.message = "{bad"; p("jsonPayload.bad", tryIt(() -> jp.getAsJsonNode()));
    }
    return out;
  }

  static Map<String, String> load(String name) throws IOException {
    Map<String, String> m = new LinkedHashMap<>();
    try (InputStream in = Jackson2FormatGoldenTest.class.getResourceAsStream(name)) {
      if (in == null) return m;
      for (String line : new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).split("\n")) {
        if (line.isEmpty()) continue;
        int i = line.indexOf('=');
        m.put(line.substring(0, i), line.substring(i + 1));
      }
    }
    return m;
  }

  @org.junit.jupiter.api.Test
  void matchesJackson2Output(@org.junit.jupiter.api.io.TempDir Path dir) throws Exception {
    Map<String, String> expected = load("jackson2-util.txt");
    // Deliberate 4.0.0 differences (see the PR): these keys are pinned to the 4.0.0 output.
    Map<String, String> overrides = load("jackson3-util-javatime.txt");
    org.assertj.core.api.Assertions.assertThat(overrides).isNotEmpty();
    org.assertj.core.api.Assertions.assertThat(expected).containsKeys(overrides.keySet().toArray(new String[0]));
    expected.putAll(overrides);
    Map<String, String> actual = new LinkedHashMap<>();
    compute(dir).forEach((k, v) -> actual.put(k, v.replace("StringNode", "TextNode")));
    org.assertj.core.api.Assertions.assertThat(expected).hasSize(68);
    org.assertj.core.api.Assertions.assertThat(actual).containsExactlyEntriesOf(expected);
  }
}

