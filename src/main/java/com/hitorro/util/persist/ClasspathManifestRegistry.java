/*
 * Copyright (c) 2006-2026 Chris Collins
 */
package com.hitorro.util.persist;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Load a curated bundle of resources from the classpath. The bundle is
 * a {@code manifest.json} listing entries; each entry names a sibling
 * resource whose body is inlined into the entry POJO on load so callers
 * get one self-contained payload per entry.
 *
 * <p>The right shape for shipping named templates, plugin manifests,
 * dataset catalogs, prompt libraries — anything where the operator
 * picks from a curated list and needs the full body without a
 * follow-up fetch.</p>
 *
 * <p>Loading is one-shot at construction: the registry snapshot is
 * built then read-only. Ship a new jar to change entries — this is
 * deliberate (curated content, not runtime config).</p>
 *
 * <p>Failure to load an individual entry (missing resource, bad
 * manifest row) is logged to stderr and skipped — the rest of the
 * registry still comes up.</p>
 *
 * <p>Extracted from {@code hitorro-mesh-pipelines}' ScheduleTemplateRegistry
 * so any hitorro service can ship template libraries the same way.</p>
 *
 * @param <T> entry POJO type
 */
public final class ClasspathManifestRegistry<T> {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final Map<String, T> byId;

    /**
     * @param manifestResource  path to a JSON array — e.g. {@code "schedule-templates/manifest.json"}
     * @param bodyDir           dir prefix for entry-referenced resources (typically the same dir as the manifest)
     * @param type              concrete entry class
     * @param idOf              identity extractor
     * @param bodyResourceOf    given an entry, returns the resource name to inline
     * @param bodyInliner       receives (entry, body-text) — sets the field on the POJO
     */
    public ClasspathManifestRegistry(String manifestResource,
                                     String bodyDir,
                                     Class<T> type,
                                     Function<T, String> idOf,
                                     Function<T, String> bodyResourceOf,
                                     BiConsumer<T, String> bodyInliner) {
        this.byId = load(manifestResource, bodyDir, type, idOf, bodyResourceOf, bodyInliner);
    }

    public List<T> all() {
        return new ArrayList<>(byId.values());
    }

    public Optional<T> get(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> load(String manifestResource,
                                           String bodyDir,
                                           Class<T> type,
                                           Function<T, String> idOf,
                                           Function<T, String> bodyResourceOf,
                                           BiConsumer<T, String> bodyInliner) {
        Map<String, T> out = new LinkedHashMap<>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = ClasspathManifestRegistry.class.getClassLoader();
        try (InputStream in = cl.getResourceAsStream(manifestResource)) {
            if (in == null) {
                System.err.println("[ClasspathManifestRegistry] no " + manifestResource
                        + " on classpath — registry disabled");
                return out;
            }
            T[] entries = (T[]) JSON.readValue(in, Array.newInstance(type, 0).getClass());
            String dir = bodyDir == null ? "" : (bodyDir.endsWith("/") ? bodyDir : bodyDir + "/");
            for (T entry : entries) {
                String id = idOf.apply(entry);
                if (id == null || id.isBlank()) {
                    System.err.println("[ClasspathManifestRegistry] skipping entry with no id");
                    continue;
                }
                String bodyRes = bodyResourceOf.apply(entry);
                if (bodyRes == null || bodyRes.isBlank()) {
                    // No body — entry is metadata-only; that's fine.
                    out.put(id, entry);
                    continue;
                }
                String body = readTextResource(cl, dir + bodyRes);
                if (body == null) {
                    System.err.println("[ClasspathManifestRegistry] " + dir + bodyRes
                            + " not found — skipping " + id);
                    continue;
                }
                bodyInliner.accept(entry, body);
                out.put(id, entry);
            }
        } catch (IOException e) {
            System.err.println("[ClasspathManifestRegistry] load failed: " + e.getMessage());
        }
        return out;
    }

    private static String readTextResource(ClassLoader cl, String path) {
        try (InputStream in = cl.getResourceAsStream(path)) {
            if (in == null) return null;
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }
}
