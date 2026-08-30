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

class NamedTextValueStoreTest {

    @Test
    void empty_when_never_set(@TempDir Path tmp) throws IOException {
        NamedTextValueStore s = new NamedTextValueStore(tmp);
        assertThat(s.get("nope")).isEmpty();
    }

    @Test
    void put_get_round_trip(@TempDir Path tmp) throws IOException {
        NamedTextValueStore s = new NamedTextValueStore(tmp);
        s.put("cursor", "2026-08-30T12:00:00Z");
        assertThat(s.get("cursor")).contains("2026-08-30T12:00:00Z");
    }

    @Test
    void survives_new_instance_over_same_dir(@TempDir Path tmp) throws IOException {
        new NamedTextValueStore(tmp).put("k", "v");
        assertThat(new NamedTextValueStore(tmp).get("k")).contains("v");
    }

    @Test
    void put_null_stores_empty_string(@TempDir Path tmp) throws IOException {
        NamedTextValueStore s = new NamedTextValueStore(tmp);
        s.put("k", null);
        assertThat(s.get("k")).contains("");   // present, but empty — distinct from absent
    }

    @Test
    void unsafe_names_are_sanitized_to_a_single_file(@TempDir Path tmp) throws IOException {
        NamedTextValueStore s = new NamedTextValueStore(tmp);
        s.put("weird/name with spaces & symbols!", "x");
        assertThat(s.get("weird/name with spaces & symbols!")).contains("x");
        try (var files = Files.list(tmp)) {
            assertThat(files.count()).isEqualTo(1);   // no directory traversal
        }
    }

    @Test
    void remove_deletes_and_makes_it_absent(@TempDir Path tmp) throws IOException {
        NamedTextValueStore s = new NamedTextValueStore(tmp);
        s.put("k", "v");
        s.remove("k");
        assertThat(s.get("k")).isEmpty();
    }
}
