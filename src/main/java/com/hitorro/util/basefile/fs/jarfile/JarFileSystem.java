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
package com.hitorro.util.basefile.fs.jarfile;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.BaseFileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * FileSystem implementation for reading from JAR/ZIP files.
 * 
 * <p>This is a read-only filesystem that allows reading entries from JAR or ZIP files.
 * Write operations are not supported.
 * 
 * <p><b>Usage:</b>
 * <pre>
 * // Create filesystem pointing to a JAR
 * JarFileSystem jarFs = new JarFileSystem(new File("myapp.jar"));
 * 
 * // Read a file from the JAR
 * JarFileFile file = jarFs.getFile("config/application.properties");
 * if (file != null && file.exists()) {
 *     try (InputStream is = file.getInputStream()) {
 *         // Read content
 *     }
 * }
 * 
 * // List all entries
 * JarFileFile[] entries = jarFs.listAllEntries();
 * </pre>
 */
public class JarFileSystem extends BaseFileSystem<JarFileFile, JarFileSystem> {
    
    private File jarFile;
    
    /**
     * Default constructor (no JAR file configured).
     * Calls to getFile() will return null until setJarFile() is called.
     */
    public JarFileSystem() {
        this.jarFile = null;
    }
    
    /**
     * Create a JarFileSystem for the specified JAR/ZIP file.
     * 
     * @param jarFile The JAR or ZIP file to read from
     */
    public JarFileSystem(File jarFile) {
        this.jarFile = jarFile;
    }
    
    /**
     * Create a JarFileSystem for the specified JAR/ZIP file path.
     * 
     * @param jarPath Path to the JAR or ZIP file
     */
    public JarFileSystem(String jarPath) {
        this.jarFile = new File(jarPath);
    }
    
    /**
     * Set the JAR file to read from.
     * 
     * @param jarFile The JAR or ZIP file
     */
    public void setJarFile(File jarFile) {
        this.jarFile = jarFile;
    }
    
    /**
     * Get the JAR file being read.
     * 
     * @return The JAR file, or null if not configured
     */
    public File getJarFile() {
        return jarFile;
    }

    @Override
    public boolean deleteFileSystem() {
        // JAR filesystems are read-only
        return false;
    }

    @Override
    public JarFileFile getFile(final BaseFile af) {
        if (af == null) {
            return null;
        }
        return getFile(af.getAbsolutePath());
    }

    @Override
    public JarFileFile getFile(final String path) {
        if (jarFile == null || !jarFile.exists()) {
            return null;
        }
        
        if (path == null) {
            return null;
        }
        
        // Normalize path (remove leading slash)
        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
        
        try (FileInputStream fis = new FileInputStream(jarFile);
             ZipInputStream zis = new ZipInputStream(fis)) {
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                
                // Match exact path or directory
                if (entryName.equals(normalizedPath) || 
                    (entryName.equals(normalizedPath + "/") && entry.isDirectory())) {
                    // Found the entry - reopen to get a fresh stream for this entry
                    return reopenForEntry(entryName);
                }
            }
            
            // Not found
            return null;
            
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public JarFileFile getFileEnsuringDir(final String path) {
        // JAR filesystems are read-only, can't create directories
        return getFile(path);
    }
    
    /**
     * List all entries in the JAR file.
     * 
     * @return Array of all JAR entries, or empty array if JAR not configured
     */
    public JarFileFile[] listAllEntries() {
        if (jarFile == null || !jarFile.exists()) {
            return new JarFileFile[0];
        }
        
        List<JarFileFile> entries = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(jarFile);
             ZipInputStream zis = new ZipInputStream(fis)) {
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // Skip directories for now
                if (!entry.isDirectory()) {
                    JarFileFile jff = reopenForEntry(entry.getName());
                    if (jff != null) {
                        entries.add(jff);
                    }
                }
            }
            
        } catch (IOException e) {
            // Return what we have
        }
        
        return entries.toArray(new JarFileFile[0]);
    }
    
    /**
     * List entries in a specific directory within the JAR.
     * 
     * @param directory The directory path (e.g., "config/" or "config")
     * @return Array of entries in that directory
     */
    public JarFileFile[] listDirectory(String directory) {
        if (jarFile == null || !jarFile.exists()) {
            return new JarFileFile[0];
        }
        
        // Normalize directory path
        String normalizedDir = directory;
        if (!normalizedDir.endsWith("/") && !normalizedDir.isEmpty()) {
            normalizedDir += "/";
        }
        if (normalizedDir.startsWith("/")) {
            normalizedDir = normalizedDir.substring(1);
        }
        
        List<JarFileFile> entries = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(jarFile);
             ZipInputStream zis = new ZipInputStream(fis)) {
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                
                // Check if entry is in this directory
                if (entryName.startsWith(normalizedDir) && !entry.isDirectory()) {
                    // Make sure it's directly in this directory (not subdirectory)
                    String relativePath = entryName.substring(normalizedDir.length());
                    if (!relativePath.contains("/")) {
                        JarFileFile jff = reopenForEntry(entryName);
                        if (jff != null) {
                            entries.add(jff);
                        }
                    }
                }
            }
            
        } catch (IOException e) {
            // Return what we have
        }
        
        return entries.toArray(new JarFileFile[0]);
    }
    
    /**
     * Reopen the JAR and position the stream at the specified entry.
     * This is necessary because ZipInputStream can only read forward.
     * 
     * @param entryName The entry to find
     * @return JarFileFile positioned at the entry, or null if not found
     */
    private JarFileFile reopenForEntry(String entryName) {
        try {
            FileInputStream fis = new FileInputStream(jarFile);
            ZipInputStream zis = new ZipInputStream(fis);
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(entryName)) {
                    return new JarFileFile(zis, entry);
                }
            }
            
            // Not found - close streams
            zis.close();
            fis.close();
            return null;
            
        } catch (IOException e) {
            return null;
        }
    }
}
