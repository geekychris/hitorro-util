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
package com.hitorro.util.log;

import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.FileUtil;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

/**
 * Adapted from the  RollingFileAppender
 * <p/>
 * A file appender that achives the log file an unlimited number of times to an archive directory as the file reaches a
 * specified maxinum size. The name of the archived file encodes the date/time that it was archived.
 * <p/>
 * <p/>
 */

public class ArchivingAppender extends FileAppender {
    /**
     * The default maximum file size is 10MB.
     */
    protected long m_maxFileSize = 10 * 1024 * 1024;

    private String m_filenamePattern;
    private File m_logDirectory;
    private File m_fileName;
    private File archiveDir;

    /**
     * The default constructor simply calls its {@link FileAppender#FileAppender parents constructor}.
     */
    private ArchivingAppender() {
        super();
    }

    /**
     * Instantiate the appender and open a new file in the log directory. The opened file will become the output
     * destination for this appender.
     * <p/>
     * If a file already exists with the filename, before opening the file the old one will be moved to an arhive
     * directory.
     */
    public ArchivingAppender(Layout layout,
                             File logDirectory,
                             String filenamePattern,
                             File archiveDirectory,
                             int maxFileSize)
            throws IOException {
        this.m_maxFileSize = maxFileSize;
        this.layout = layout;
        //not appending.
        m_filenamePattern = filenamePattern;
        m_logDirectory = logDirectory;
        // ensure dir exists
        m_logDirectory.mkdir();

        generateFileName();
        this.archiveDir = archiveDirectory;
        archiveDir.mkdir();
        // move the existing file if it exists.
        if (this.m_fileName.exists()) {
            m_fileName.renameTo(this.getTargetFile());
        }
        this.setFile(m_fileName, false, bufferedIO, bufferSize);
    }

    private void generateFileName() {
        m_fileName = new File(m_logDirectory, Fmt.S("%s.%s", m_filenamePattern, "log"));
    }

    private File getTargetFile() {
        return FileUtil.getDatedFileFromPattern(this.archiveDir, m_filenamePattern, "log");
    }

    /**
     * Get the maximum size that the output file is allowed to reach before being rolled over to backup files.
     *
     * @since 1.1
     */
    public long getMaximumFileSize() {
        return m_maxFileSize;
    }

    /**
     * Set the maximum size that the ou tput file is allowed to reach before being rolled over to backup files.
     * <p/>
     * <p/>
     * This method is equivalent to {@link #setMaxFileSize} except that it is required for differentiating the setter
     * taking a <code>long</code> argument from the setter taking a <code>String</code> argument by the JavaBeans {@link
     * java.beans.Introspector Introspector}.
     *
     * @see #setMaxFileSize(String)
     */
    public void setMaximumFileSize(long maxFileSize) {
        this.m_maxFileSize = maxFileSize;
    }

    /**
     * Closes the existing file and moves it to an archive directory, using a filename that encodes the time of the
     * rollover. Creates a new file to receive subsequent log events.
     */
    protected // synchronization not necessary since doAppend is alreasy synched
    void rollOver() {
        assert Thread.holdsLock(this);
        File target;

        // Create new archive filename, and delete it's file in the unlikely case that it exists.
        target = getTargetFile();
        if (target.exists()) {
            target.delete();
        }
        LogLog.debug("Renaming file " + this.m_fileName + " to " + target);
        this.closeFile();
        if (!this.m_fileName.renameTo(target)) {
            LogLog.error("Unable to rename: " + m_fileName.getAbsolutePath() + " to: " + target.getAbsolutePath());
        }
        generateFileName();
        try {
            // This will also close the file. This is OK since multiple
            // close operations are safe.
            this.setFile(m_fileName, false, bufferedIO, bufferSize);
        } catch (IOException e) {
            LogLog.error("setFile(" + m_fileName.getName() + ", false) call failed.", e);
        }
    }

    public synchronized void setFile(File file, boolean append,
                                     boolean bufferedIO, int bufferSize) throws IOException {
        super.setFile(file.getAbsolutePath(), append, this.bufferedIO, this.bufferSize);
        if (append) {
            ((CountingQuietWriter) qw).setCount(file.length());
        }
    }

    /**
     * Set the maximum size that the output file is allowed to reach before being rolled over to backup files.
     * <p/>
     * <p/>
     * In configuration files, the <b>MaxFileSize</b> option takes an long integer in the range 0 - 2^63. You can
     * specify the value with the suffixes "KB", "MB" or "GB" so that the integer is interpreted being expressed
     * respectively in kilobytes, megabytes or gigabytes. For example, the value "10KB" will be interpreted as 10240.
     */
    public void setMaxFileSize(String value) {
        m_maxFileSize = OptionConverter.toFileSize(value, m_maxFileSize + 1);
    }

    @Override
    protected void setQWForFiles(Writer writer) {
        this.qw = new CountingQuietWriter(writer, errorHandler);
    }

    /**
     * This method differentiates RollingFileAppender from its super class.
     *
     * @since 0.9.0
     */
    @Override
    protected void subAppend(LoggingEvent event) {
        super.subAppend(event);
        if ((fileName != null)
                && ((CountingQuietWriter) qw).getCount() >= m_maxFileSize) {
            this.rollOver();
        }
    }
}

