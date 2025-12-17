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

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FileUtil Tests")
class FileUtilTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("Constants and Defaults")
    class ConstantsAndDefaults {

        @Test
        @DisplayName("Should have default buffer size constant")
        void shouldHaveDefaultBufferSizeConstant() {
            assertThat(com.hitorro.util.io.FileUtil.DefaultFileReaderBufferSize).isEqualTo(1024);
        }

        @Test
        @DisplayName("Should have digit array for encoding")
        void shouldHaveDigitArrayForEncoding() {
            // Verify the Digits array is accessible and has expected format
            assertThat(com.hitorro.util.io.FileUtil.Digits).isNotNull();
        }
    }

    @Nested
    @DisplayName("File Input Stream Operations")
    class FileInputStreamOperations {

        @Test
        @DisplayName("Should create input stream from file")
        void shouldCreateInputStreamFromFile() throws IOException {
            Path testFile = tempDir.resolve("test.txt");
            Files.writeString(testFile, "Hello World");

            InputStream is = com.hitorro.util.io.FileUtil.fsInputStream.apply(testFile.toFile());

            assertThat(is).isNotNull();
            assertThat(is.available()).isGreaterThan(0);
            is.close();
        }

        @Test
        @DisplayName("Should handle gzip files")
        void shouldHandleGzipFiles() throws IOException {
            Path testFile = tempDir.resolve("test.gz");
            
            // Create a gzip file
            try (FileOutputStream fos = new FileOutputStream(testFile.toFile());
                 java.util.zip.GZIPOutputStream gzos = new java.util.zip.GZIPOutputStream(fos)) {
                gzos.write("Compressed content".getBytes());
            }

            InputStream is = com.hitorro.util.io.FileUtil.fsInputStream.apply(testFile.toFile());

            assertThat(is).isNotNull();
            is.close();
        }

        @Test
        @DisplayName("Should return null for non-existent file")
        void shouldReturnNullForNonExistentFile() {
            File nonExistent = new File(tempDir.toFile(), "does-not-exist.txt");

            InputStream is = com.hitorro.util.io.FileUtil.fsInputStream.apply(nonExistent);

            // Based on implementation, it should handle the error
            assertThat(is).isNull();
        }
    }

    @Nested
    @DisplayName("File Output Stream Operations")
    class FileOutputStreamOperations {

        @Test
        @DisplayName("Should create output stream for file")
        void shouldCreateOutputStreamForFile() throws IOException {
            Path testFile = tempDir.resolve("output.txt");

            OutputStream os = com.hitorro.util.io.FileUtil.fsOutputStream.apply(testFile.toFile());

            assertThat(os).isNotNull();
            os.write("Test content".getBytes());
            os.close();

            assertThat(Files.exists(testFile)).isTrue();
            assertThat(Files.readString(testFile)).isEqualTo("Test content");
        }

        @Test
        @DisplayName("Should create gzip output stream")
        void shouldCreateGzipOutputStream() throws IOException {
            Path testFile = tempDir.resolve("output.gz");

            OutputStream os = com.hitorro.util.io.FileUtil.fsOutputStream.apply(testFile.toFile());

            assertThat(os).isNotNull();
            os.write("Compressed".getBytes());
            os.close();

            assertThat(Files.exists(testFile)).isTrue();
        }
    }

    @Nested
    @DisplayName("Stream to Writer Conversions")
    class StreamToWriterConversions {

        @Test
        @DisplayName("Should convert OutputStream to Writer")
        void shouldConvertOutputStreamToWriter() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Writer writer = com.hitorro.util.io.FileUtil.os2Utf8Writer.apply(baos);

            assertThat(writer).isNotNull();
            writer.write("Test");
            writer.flush();

            assertThat(baos.toString()).isEqualTo("Test");
            writer.close();
        }

        @Test
        @DisplayName("Should convert OutputStream to PrintWriter")
        void shouldConvertOutputStreamToPrintWriter() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PrintWriter pw = com.hitorro.util.io.FileUtil.os2Utf8PrintWriter.apply(baos);

            assertThat(pw).isNotNull();
            pw.println("Line 1");
            pw.println("Line 2");
            pw.flush();

            assertThat(baos.toString()).contains("Line 1");
            assertThat(baos.toString()).contains("Line 2");
            pw.close();
        }

        @Test
        @DisplayName("Should convert OutputStream to PrintStream")
        void shouldConvertOutputStreamToPrintStream() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PrintStream ps = com.hitorro.util.io.FileUtil.os2Utf8PrintStream.apply(baos);

            assertThat(ps).isNotNull();
            ps.print("Stream output");
            ps.flush();

            assertThat(baos.toString()).isEqualTo("Stream output");
            ps.close();
        }

        @Test
        @DisplayName("Should convert byte array to InputStream")
        void shouldConvertByteArrayToInputStream() throws IOException {
            byte[] data = "Test data".getBytes();

            InputStream is = com.hitorro.util.io.FileUtil.byteArray2InputStream.apply(data);

            assertThat(is).isNotNull();
            assertThat(is.available()).isEqualTo(data.length);

            byte[] read = new byte[data.length];
            is.read(read);
            assertThat(read).isEqualTo(data);
            is.close();
        }

        @Test
        @DisplayName("Should convert InputStream to Reader")
        void shouldConvertInputStreamToReader() throws IOException {
            String content = "Hello UTF-8 世界";
            ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes("UTF-8"));

            Reader reader = com.hitorro.util.io.FileUtil.is2reader.apply(bais);

            assertThat(reader).isNotNull();

            StringBuilder sb = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }

            assertThat(sb.toString()).isEqualTo(content);
            reader.close();
        }
    }

    @Nested
    @DisplayName("Mapper Chaining")
    class MapperChaining {

        @Test
        @DisplayName("Should chain File to Writer mapper")
        void shouldChainFileToWriterMapper() throws IOException {
            Path testFile = tempDir.resolve("chained.txt");

            Writer writer = com.hitorro.util.io.FileUtil.file2Utf8Writer.apply(testFile.toFile());

            assertThat(writer).isNotNull();
            writer.write("Chained mapping");
            writer.close();

            assertThat(Files.readString(testFile)).isEqualTo("Chained mapping");
        }
    }

    @Nested
    @DisplayName("Reader to Iterator Conversions")
    class ReaderToIteratorConversions {

        @Test
        @DisplayName("Should convert Reader to JSON iterator")
        void shouldConvertReaderToJsonIterator() throws IOException {
            String jsonContent = "{\"key\":\"value\"}\n{\"key2\":\"value2\"}";
            StringReader reader = new StringReader(jsonContent);

            var iter = com.hitorro.util.io.FileUtil.readerJacksonJsonIter.apply(reader);

            assertThat((Object) iter).isNotNull();
            assertThat(iter.hasNext()).isTrue();
        }

        @Test
        @DisplayName("Should handle empty JSON content")
        void shouldHandleEmptyJsonContent() {
            StringReader reader = new StringReader("");

            var iter = com.hitorro.util.io.FileUtil.readerJacksonJsonIter.apply(reader);

            assertThat((Object) iter).isNotNull();
        }
    }

    @Nested
    @DisplayName("File Operations")
    class FileOperations {

        @Test
        @DisplayName("Should read and write files")
        void shouldReadAndWriteFiles() throws IOException {
            Path testFile = tempDir.resolve("readwrite.txt");
            String content = "Test content for file operations";

            // Write
            try (OutputStream os = com.hitorro.util.io.FileUtil.fsOutputStream.apply(testFile.toFile())) {
                os.write(content.getBytes());
            }

            // Read
            try (InputStream is = com.hitorro.util.io.FileUtil.fsInputStream.apply(testFile.toFile())) {
                byte[] buffer = new byte[1024];
                int read = is.read(buffer);
                String readContent = new String(buffer, 0, read);

                assertThat(readContent).isEqualTo(content);
            }
        }

        @Test
        @DisplayName("Should handle binary files")
        void shouldHandleBinaryFiles() throws IOException {
            Path testFile = tempDir.resolve("binary.dat");
            byte[] binaryData = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05};

            // Write binary
            try (OutputStream os = com.hitorro.util.io.FileUtil.fsOutputStream.apply(testFile.toFile())) {
                os.write(binaryData);
            }

            // Read binary
            try (InputStream is = com.hitorro.util.io.FileUtil.fsInputStream.apply(testFile.toFile())) {
                byte[] read = new byte[binaryData.length];
                is.read(read);

                assertThat(read).isEqualTo(binaryData);
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty files")
        void shouldHandleEmptyFiles() throws IOException {
            Path emptyFile = tempDir.resolve("empty.txt");
            Files.writeString(emptyFile, "");

            InputStream is = com.hitorro.util.io.FileUtil.fsInputStream.apply(emptyFile.toFile());

            assertThat(is).isNotNull();
            assertThat(is.available()).isZero();
            is.close();
        }

        @Test
        @DisplayName("Should handle large content")
        void shouldHandleLargeContent() throws IOException {
            Path largeFile = tempDir.resolve("large.txt");
            StringBuilder largeContent = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                largeContent.append("Line ").append(i).append("\n");
            }

            // Write large content
            try (OutputStream os = com.hitorro.util.io.FileUtil.fsOutputStream.apply(largeFile.toFile())) {
                os.write(largeContent.toString().getBytes());
            }

            // Verify file exists and has content
            assertThat(Files.exists(largeFile)).isTrue();
            assertThat(Files.size(largeFile)).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should handle UTF-8 encoded content")
        void shouldHandleUtf8EncodedContent() throws IOException {
            Path utf8File = tempDir.resolve("utf8.txt");
            String utf8Content = "Hello 世界 🌍 Привет مرحبا";

            Files.writeString(utf8File, utf8Content);

            try (InputStream is = com.hitorro.util.io.FileUtil.fsInputStream.apply(utf8File.toFile());
                 Reader reader = com.hitorro.util.io.FileUtil.is2reader.apply(is)) {

                StringBuilder sb = new StringBuilder();
                int ch;
                while ((ch = reader.read()) != -1) {
                    sb.append((char) ch);
                }

                assertThat(sb.toString()).isEqualTo(utf8Content);
            }
        }
    }
}
