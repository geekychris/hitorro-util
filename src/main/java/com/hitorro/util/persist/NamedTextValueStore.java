/*
 * Copyright (c) 2006-2026 Chris Collins
 */
package com.hitorro.util.persist;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * One small text file per named key, atomic move-with-replace semantics.
 *
 * <p>The right shape for anything that stores a short opaque string per
 * entity — pipeline scheduler checkpoints, ETL watermarks, per-consumer
 * offsets, feature flags, last-seen markers. Value is UTF-8 text; the
 * caller owns the semantic (ISO instant, unix seconds, monotonic id,
 * whatever).</p>
 *
 * <p>Per-key {@link ReentrantLock} serialises writes to the same file
 * so concurrent {@link #put} on the same key doesn't produce a torn
 * write. Cross-key writes proceed in parallel. Reads are lock-free
 * (rely on the atomic move for consistency — a reader sees either the
 * old value or the new one, never a half-written file).</p>
 *
 * <p>Not safe against multiple processes writing the same directory
 * — one process, many threads is the intended use.</p>
 *
 * <p>File naming: names are sanitised — anything outside
 * {@code [A-Za-z0-9._-]} is replaced with {@code _}. That means
 * {@code "foo/bar"} and {@code "foo_bar"} collide; if callers can
 * generate arbitrary names, hash them first.</p>
 *
 * <p>Extracted from {@code hitorro-mesh-pipelines}' CheckpointStore
 * so any hitorro service can get durable per-name text storage
 * without pulling in the mesh runtime.</p>
 */
public class NamedTextValueStore {

    private final Path root;
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    /** Creates the given directory if absent — one instance per store dir. */
    public NamedTextValueStore(Path root) throws IOException {
        this.root = root;
        Files.createDirectories(root);
    }

    /** Absent when the key was never written. Empty {@code Optional.of("")}
     *  when the key was explicitly stored as empty. */
    public Optional<String> get(String name) {
        Path p = fileFor(name);
        if (!Files.exists(p)) return Optional.empty();
        try { return Optional.of(Files.readString(p, StandardCharsets.UTF_8).stripTrailing()); }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }

    /** Overwrites atomically. Null is stored as the empty string —
     *  distinct from "never set" (which is {@link Optional#empty}). */
    public void put(String name, String value) throws IOException {
        Path p = fileFor(name);
        Path tmp = p.resolveSibling(p.getFileName() + ".tmp");
        ReentrantLock l = locks.computeIfAbsent(name, k -> new ReentrantLock());
        l.lock();
        try {
            Files.writeString(tmp, value == null ? "" : value, StandardCharsets.UTF_8);
            Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally { l.unlock(); }
    }

    /** No-op when the key was never written. */
    public void remove(String name) throws IOException {
        Files.deleteIfExists(fileFor(name));
    }

    /** Root directory — useful for tests and diagnostics. */
    public Path root() { return root; }

    private Path fileFor(String name) {
        String safe = name.replaceAll("[^A-Za-z0-9._-]", "_");
        return root.resolve(safe + ".txt");
    }
}
