/*
 * Copyright (c) 2006-2026 Chris Collins
 */
package com.hitorro.util.persist;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class JsonFileEntityStoreTest {

    /** Test-only entity — public fields so Jackson round-trips it without any annotations. */
    public static final class Thing {
        public String id;
        public String label;
        public int    n;

        public Thing() { }
        public Thing(String id, String label, int n) { this.id = id; this.label = label; this.n = n; }
    }

    @Test
    void round_trip_persists_and_reloads(@TempDir Path tmp) throws IOException {
        Path f = tmp.resolve("things.json");
        JsonFileEntityStore<Thing> s1 = new JsonFileEntityStore<>(f, Thing.class, t -> t.id);
        s1.put(new Thing("a", "first", 1));
        s1.put(new Thing("b", "second", 2));

        assertThat(Files.exists(f)).isTrue();
        JsonFileEntityStore<Thing> s2 = new JsonFileEntityStore<>(f, Thing.class, t -> t.id);
        assertThat(s2.get("a")).isPresent().get().extracting(t -> t.label).isEqualTo("first");
        assertThat(s2.all()).extracting(t -> t.id).containsExactly("a", "b");   // insertion order
    }

    @Test
    void update_mutates_in_place(@TempDir Path tmp) throws IOException {
        JsonFileEntityStore<Thing> s = new JsonFileEntityStore<>(
                tmp.resolve("t.json"), Thing.class, t -> t.id);
        s.put(new Thing("x", "before", 1));
        Thing after = s.update("x", t -> { t.label = "after"; t.n = 99; });
        assertThat(after.label).isEqualTo("after");
        assertThat(after.n).isEqualTo(99);
    }

    @Test
    void update_throws_for_unknown_id(@TempDir Path tmp) throws IOException {
        JsonFileEntityStore<Thing> s = new JsonFileEntityStore<>(
                tmp.resolve("t.json"), Thing.class, t -> t.id);
        try {
            s.update("nope", t -> {});
            org.assertj.core.api.Assertions.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) { /* ok */ }
    }

    @Test
    void remove_returns_true_only_when_present(@TempDir Path tmp) throws IOException {
        JsonFileEntityStore<Thing> s = new JsonFileEntityStore<>(
                tmp.resolve("t.json"), Thing.class, t -> t.id);
        s.put(new Thing("keep", "k", 0));
        s.put(new Thing("gone", "g", 0));
        assertThat(s.remove("gone")).isTrue();
        assertThat(s.remove("gone")).isFalse();
        assertThat(s.all()).extracting(t -> t.id).containsExactly("keep");
    }

    @Test
    void put_rejects_blank_id(@TempDir Path tmp) throws IOException {
        JsonFileEntityStore<Thing> s = new JsonFileEntityStore<>(
                tmp.resolve("t.json"), Thing.class, t -> t.id);
        try {
            s.put(new Thing(null, "no-id", 0));
            org.assertj.core.api.Assertions.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) { /* ok */ }
    }
}
