package ht.util.basefile.fs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Iterator;

public class TestF implements Path {
    @Override
    public FileSystem getFileSystem() {
        return null;
    }

    @Override
    public boolean isAbsolute() {
        return false;
    }

    @Override
    public Path getRoot() {
        return null;
    }

    @Override
    public Path getFileName() {
        return null;
    }

    @Override
    public Path getParent() {
        return null;
    }

    @Override
    public int getNameCount() {
        return 0;
    }

    @Override
    public Path getName(final int index) {
        return null;
    }

    @Override
    public Path subpath(final int beginIndex, final int endIndex) {
        return null;
    }

    @Override
    public boolean startsWith(final Path other) {
        return false;
    }

    @Override
    public boolean startsWith(final String other) {
        return false;
    }

    @Override
    public boolean endsWith(final Path other) {
        return false;
    }

    @Override
    public boolean endsWith(final String other) {
        return false;
    }

    @Override
    public Path normalize() {
        return null;
    }

    @Override
    public Path resolve(final Path other) {
        return null;
    }

    @Override
    public Path resolve(final String other) {
        return null;
    }

    @Override
    public Path resolveSibling(final Path other) {
        return null;
    }

    @Override
    public Path resolveSibling(final String other) {
        return null;
    }

    @Override
    public Path relativize(final Path other) {
        return null;
    }

    @Override
    public URI toUri() {
        return null;
    }

    @Override
    public Path toAbsolutePath() {
        return null;
    }

    @Override
    public Path toRealPath(final LinkOption... options) throws IOException {
        return null;
    }

    @Override
    public File toFile() {
        return null;
    }

    @Override
    public WatchKey register(final WatchService watcher, final WatchEvent.Kind<?>[] events, final WatchEvent.Modifier... modifiers) throws IOException {
        return null;
    }

    @Override
    public WatchKey register(final WatchService watcher, final WatchEvent.Kind<?>... events) throws IOException {
        return null;
    }

    @Override
    public Iterator<Path> iterator() {
        return null;
    }

    @Override
    public int compareTo(final Path other) {
        return 0;
    }
}
