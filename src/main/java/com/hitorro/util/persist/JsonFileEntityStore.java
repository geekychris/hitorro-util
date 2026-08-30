/*
 * Copyright (c) 2006-2026 Chris Collins
 */
package com.hitorro.util.persist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Durable in-memory-cached CRUD over a single JSON file.
 * Reads on construction; writes re-serialise the whole registry with an
 * atomic move-with-replace on every mutation. Cheap given typical counts
 * are small (dozens at most — think saved queries, schedules, webhook
 * configs, alert rules).
 *
 * <p>Not process-safe (no file locking) — one JVM writing, many threads
 * reading. Internally coordinated via a {@link ReentrantReadWriteLock}
 * so pollers and mutators can share one instance. Reader iteration is
 * defensive-copy so callers can't mutate the in-memory list.</p>
 *
 * <p>Entities must have a string identity — supplied by the caller as
 * a {@code Function<T,String>}. Insertion order is preserved for
 * stable diffs and predictable listing.</p>
 *
 * <p>Extracted from {@code hitorro-mesh-pipelines}' ScheduleStore so
 * any hitorro service can get durable POJO-list persistence without
 * pulling in the mesh runtime.</p>
 *
 * @param <T> entity type — Jackson-serialisable POJO
 */
public class JsonFileEntityStore<T> {

    private static final ObjectMapper JSON = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final Path file;
    private final Class<T> type;
    private final Function<T, String> idOf;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, T> byId = new LinkedHashMap<>();

    /**
     * @param file  destination JSON file; parent dir must exist (or be
     *              creatable) — the constructor ensures it
     * @param type  concrete entity class — required for Jackson
     *              deserialisation
     * @param idOf  identity extractor; must return a non-null, non-blank
     *              string for every well-formed entity
     */
    public JsonFileEntityStore(Path file, Class<T> type, Function<T, String> idOf) throws IOException {
        this.file = file;
        this.type = type;
        this.idOf = idOf;
        if (file.getParent() != null) Files.createDirectories(file.getParent());
        load();
    }

    public List<T> all() {
        lock.readLock().lock();
        try { return new ArrayList<>(byId.values()); }
        finally { lock.readLock().unlock(); }
    }

    public Optional<T> get(String id) {
        lock.readLock().lock();
        try { return Optional.ofNullable(byId.get(id)); }
        finally { lock.readLock().unlock(); }
    }

    /** Insert or replace by id. */
    public T put(T entity) throws IOException {
        String id = idOf.apply(entity);
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("entity id is required");
        lock.writeLock().lock();
        try {
            byId.put(id, entity);
            persist();
            return entity;
        } finally { lock.writeLock().unlock(); }
    }

    /** {@code true} when something was removed, {@code false} when the id was absent. */
    public boolean remove(String id) throws IOException {
        lock.writeLock().lock();
        try {
            if (byId.remove(id) == null) return false;
            persist();
            return true;
        } finally { lock.writeLock().unlock(); }
    }

    /** In-place mutation with atomic disk write. Throws {@link IllegalArgumentException}
     *  when the id is unknown — the caller almost always wants to react to that. */
    public T update(String id, Consumer<T> mutator) throws IOException {
        lock.writeLock().lock();
        try {
            T entity = byId.get(id);
            if (entity == null) throw new IllegalArgumentException("no entity: " + id);
            mutator.accept(entity);
            persist();
            return entity;
        } finally { lock.writeLock().unlock(); }
    }

    /** File path — for tests + diagnostics. */
    public Path file() { return file; }

    // ---- IO ----------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void load() throws IOException {
        if (!Files.exists(file)) return;
        T[] arr = (T[]) JSON.readValue(Files.readAllBytes(file), Array.newInstance(type, 0).getClass());
        for (T e : arr) {
            String id = idOf.apply(e);
            if (id != null && !id.isBlank()) byId.put(id, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void persist() throws IOException {
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        T[] arr = byId.values().toArray((T[]) Array.newInstance(type, 0));
        Files.write(tmp, JSON.writeValueAsBytes(arr));
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
}
