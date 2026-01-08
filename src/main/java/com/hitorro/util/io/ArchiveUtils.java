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
package com.hitorro.util.io;

import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarException;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;

/**
 * <p/>
 * Code adapted from numerous sources including ant.
 * <p/>
 * Provides mechanism for jaring and unjaring a directory, including a manifest and variable compression levels.
 */
public class ArchiveUtils {
    public static final int COMPRESS_DEFAULT = -1;
    /**
     * Directory separator
     */
    private static final String SLASH = File.separator;
    /**
     * Name of directory holding manifest file
     */
    private static final String MAN_DIR = "META-INF";
    public static final String ManDirPath = MAN_DIR + SLASH;
    /**
     * Relative path to the manifest file starting at MAN_DIR
     */
    private static final String MAN_FILE_PATH = MAN_DIR + SLASH + "MANIFEST.MF";
    // byte buffer size
    private static final int BYTE_BUFSIZE = (32 * 1024);

    public static final void jar(File jarFile,
                                 File dir,
                                 boolean includeSrc)
            throws IOException {
        jar(jarFile, dir, CompressionLevel.COMPRESS_DEFAULT, includeSrc);
    }

    /**
     * Extracts all files in a JAR file.
     *
     * @param jarFile the JAR file to unjar.
     * @param dir     the destination directory to unjar in
     * @throws java.io.IOException        if cannot unjar <code>fileName</code>.
     * @throws java.util.jar.JarException if error extracting files from JAR file.
     */
    public static final void extract(File jarFile, File dir)
            throws IOException, JarException {
        if (!(jarFile.getName().endsWith(".jar") || jarFile.getName().endsWith(".zip"))) {
            throw new JarException(Fmt.S("Not a zip file: %s", jarFile.getAbsolutePath()));
        }

        // process all entries in that JAR file
        JarFile jar = new JarFile(jarFile);
        Enumeration all = jar.entries();
        while (all.hasMoreElements()) {
            getEntry(dir, jar, ((JarEntry) (all.nextElement())));
        }

        jar.close();
    }

    /**
     * Gets one file <code>entry</code> from <code>jarFile</code>.
     *
     * @param jarFile the JAR file reference to retrieve <code>entry</code> from.
     * @param entry   the file from the JAR to extract.
     * @throws IOException if error trying to read entry.
     */
    private static final void getEntry(File parentDir, JarFile jarFile, JarEntry entry)
            throws IOException {
        String entryName = entry.getName();
        // if a directory, mkdir it (remember to create intervening subdirectories if needed!)
        if (entryName.endsWith("/")) {
            new File(parentDir, entryName).mkdirs();
            return;
        }

        File f = new File(parentDir, entryName);

        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }

        // Must be a file; create output stream to the file
        FileOutputStream fostream = new FileOutputStream(f);
        InputStream istream = jarFile.getInputStream(entry);

        // extract files
        int n = 0;
        byte[] buffer = new byte[BYTE_BUFSIZE];
        while ((n = istream.read(buffer)) > 0) {
            fostream.write(buffer, 0, n);
        }

        try {
            istream.close();
            fostream.close();
        } catch (IOException e) {
            // do nothing
        }
    }

    /**
     * Creates a JAR file.  If the JAR file already exists, it will be overwritten. This implicitly does the equivelent
     * of jar -C <dir> i.e it only includes the relative name of the files (relative to the @param dir) and dir.
     *
     * @param dir        the directory to jar
     * @param jarFile    the JAR file to create.
     * @param includeSrc if set to true, content is contained in the jar within the source directory
     * @throws IOException  if cannot create JAR file.
     * @throws JarException if error putting files into JAR file.
     */
    public static final void jar(File jarFile,
                                 File dir,
                                 CompressionLevel compressionLevel,
                                 boolean includeSrc)
            throws IOException, JarException {
        // get all files to include in JAR file, except for manifest
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludeSrcDir(includeSrc);

        String parentDir = scanner.scan(dir);
        String[] directories = scanner.getIncludedDirectories();
        String[] files = scanner.getIncludedFiles();

        // create JAR file

        FileOutputStream fostream = new FileOutputStream(jarFile);
        JarOutputStream jostream = new JarOutputStream(fostream);

        if (compressionLevel != CompressionLevel.COMPRESS_DEFAULT) {
            jostream.setLevel(compressionLevel.getLevel());
        }

        // create manifest directory and manifest
        byte[] buffer = new byte[BYTE_BUFSIZE];

        if ((new File(dir, MAN_DIR)).exists()) {
            putManifest(dir, jostream, buffer);
        }

        //create subdirectories
        for (int i = 0; i < directories.length; i++) {
            putEntry(parentDir, directories[i] + SLASH, jostream, buffer);
        }

        // create files
        for (int i = 0; i < files.length; i++) {
            putEntry(parentDir, files[i], jostream, buffer);
        }

        jostream.finish();

        try {
            jostream.close();
            fostream.close();
        } catch (IOException e) {
            // do nothing
        }
    }

    /**
     * Puts manifest file into a JAR file.
     *
     * @param jostream the JAR file to put manifest into.
     * @throws IOException if error trying to write entry.
     */
    private static final void putManifest(File dir,
                                          JarOutputStream jostream,
                                          byte[] buffer)
            throws IOException {

        /**
         *  begin borrowed code from Ant Jar and Zip classes
         *  put the META-INF directory into JAR file.
         */


        JarEntry manifestDirEntry = new JarEntry(ManDirPath.replace(File.separatorChar, '/'));
        manifestDirEntry.setTime(System.currentTimeMillis());
        manifestDirEntry.setSize(0);
        manifestDirEntry.setMethod(JarEntry.STORED);

        // This is faintly ridiculous - empty CRC value
        manifestDirEntry.setCrc((new CRC32()).getValue());
        jostream.putNextEntry(manifestDirEntry);

        // now put the manifest file into JAR file.
        JarEntry manifestFileEntry = new JarEntry(MAN_FILE_PATH.replace(File.separatorChar, '/'));
        manifestFileEntry.setTime(System.currentTimeMillis());
        FileInputStream fistream = new FileInputStream(new File(dir, MAN_FILE_PATH));
        jostream.putNextEntry(manifestFileEntry);

        int n = 0;
        while ((n = fistream.read(buffer)) >= 0) {
            jostream.write(buffer, 0, n);
        }
        jostream.closeEntry();

        // end borrowed code from Ant Jar and Zip classes
        try {
            fistream.close();
        } catch (IOException e) {
            // do nothing
        }
    }

    /**
     * Puts a file into a JAR file.
     *
     * @param fileName the file to put in the JAR.
     * @param jostream the JAR file to put <code>fileName</code> into.
     * @param buffer   the byte buffer used to read the file contents.
     * @throws IOException if error trying to flushToDisk entry.
     */
    private static final void putEntry(String dir,
                                       String fileName,
                                       JarOutputStream jostream,
                                       byte[] buffer)
            throws IOException {
        // prepare fileName for entry into JAR file
        String entryName = fileName.replace(File.separatorChar, '/');

        // put directory (remember to create intervening subdirectories if needed!)
        if (entryName.endsWith("/")) {
            jostream.putNextEntry(new JarEntry(entryName));
            jostream.closeEntry();
            return;
        }

        // put file
        FileInputStream fistream = new FileInputStream(new File(dir, fileName));
        jostream.putNextEntry(new JarEntry(entryName));
        int n;

        // now read and flushToDisk the JAR entry data.
        while ((n = fistream.read(buffer)) >= 0) {
            jostream.write(buffer, 0, n);
        }

        jostream.closeEntry();

        try {
            fistream.close();
        } catch (IOException e) {
            // do nothing
        }
    }

    public static final File expandIfZipped(File f) {
        String ext = FileUtil.getFileExtension(f);
        if (ext.equals("jar") || ext.equals("zip")) {
            File targetDir = new File(f.getParent(), FileUtil.getFileNameSansExtension(f));

            try {
                extract(f, targetDir);
            } catch (IOException e) {
                Log.util.error("%s %e", e, e);
                return null;
            }
            return targetDir;
        }
        return f;
    }

    public enum CompressionLevel {
        COMPRESS_DEFAULT(-1),
        COMPRESS_NONE(0),
        COMPRESS_MEDIUM(5),
        COMPRESS_MAX(9);

        private int m_level;

        CompressionLevel(int level) {
            m_level = level;
        }

        public int getLevel() {
            return m_level;
        }

    }
}
