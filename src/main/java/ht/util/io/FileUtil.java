package ht.util.io;

import com.fasterxml.jackson.databind.JsonNode;
import ht.jsontypesystem.JVS;
import ht.jsontypesystem.Json2JVSMapper;
import ht.util.core.Console;
import ht.util.core.*;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.JSONIterator;
import ht.util.core.iterator.LineReaderIterator;
import ht.util.core.iterator.StreamTokenizerIterator;
import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;
import ht.util.io.filefilters.IsDirectoryFilenameFilter;
import ht.util.io.filefilters.StartsWithFilenameFilter;
import ht.util.json.JSONElement;
import ht.util.json.iterators.HTJSONIterator;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.*;


/**
 * General File utility functions, that are generic enough for even your granpa to use!
 *
 * @author ccollins
 */
public class FileUtil {
    public static final int DefaultFileReaderBufferSize = 1024;
    final static char[] Digits = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'
    };
    /**
     * Diff reasons in plain english.
     */
    private static final String[] BinaryDiffReasons =
            {"Files Match", "file a does not exist", "file b does not exist",
                    "file sizes differ", "byte level differences"};
    /**
     * ********************** Mappers ****************************
     */
    public static BaseMapper<File, InputStream> fsInputStream = new BaseMapper<File, InputStream>() {
        public InputStream apply(File bf) {
            InputStream is = null;
            try {
                is = FileUtil.getBufferedFileInputStream(bf);
                if (bf.getName().endsWith("gz")) {
                    is = new GZIPInputStream(is);
                }
            } catch (IOException e) {
                Log.filesystem.error("Unable to create stream for basefile %s %e", e, e);
                return null;
            }
            return is;
        }
    };
    public static BaseMapper<File, OutputStream> fsOutputStream = new BaseMapper<File, OutputStream>() {
        public OutputStream apply(File bf) {
            OutputStream os = null;
            try {
                os = FileUtil.getBufferedFileOutputStream(bf);
                if (bf.getName().endsWith("gz")) {
                    os = new GZIPOutputStream(os);
                }
            } catch (IOException e) {
                Log.filesystem.error("Unable to create stream for basefile %s %e", e, e);
                return null;
            }
            return os;
        }
    };
    public static BaseMapper<OutputStream, Writer> os2Utf8Writer = new BaseMapper<OutputStream, Writer>() {
        public Writer apply(OutputStream os) {
            return new BufferedWriter(new OutputStreamWriter(os));
        }
    };
    public static BaseMapper<OutputStream, PrintWriter> os2Utf8PrintWriter = new BaseMapper<OutputStream, PrintWriter>() {
        public PrintWriter apply(OutputStream os) {
            return new PrintWriter(new OutputStreamWriter(os));
        }
    };
    public static BaseMapper<OutputStream, PrintStream> os2Utf8PrintStream = new BaseMapper<OutputStream, PrintStream>() {
        public PrintStream apply(OutputStream os) {
            return new PrintStream(os);
        }
    };
    public static BaseMapper<byte[], InputStream> byteArray2InputStream = new BaseMapper<byte[], InputStream>() {

        @Override
        public InputStream apply(final byte[] e) {
            return new ByteArrayInputStream(e);
        }
    };
    public static BaseMapper<File, Writer> file2Utf8Writer = fsOutputStream.combine(os2Utf8Writer);
    public static BaseMapper<InputStream, Reader> is2reader = new BaseMapper<InputStream, Reader>() {
        public Reader apply(InputStream is) {
            try {
                return new InputStreamReader(is, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.filesystem.error("Unable to open file for reading %s %e", e, e);
            }
            return null;
        }
    };
    public static BaseMapper<Reader, AbstractIterator<JsonNode>> readerJacksonJsonIter = new BaseMapper<Reader, AbstractIterator<JsonNode>>() {
        public AbstractIterator<JsonNode> apply(Reader r) {
            return new JSONIterator(r);
        }
    };
    public static BaseMapper<Reader, AbstractIterator<JVS>> readerJVSIter = new BaseMapper<Reader, AbstractIterator<JVS>>() {
        public AbstractIterator<JVS> apply(Reader r) {
            return new JSONIterator(r).map(Json2JVSMapper.me);
        }
    };

    /*

        write mode
          Create a private (copy-on-write) memory-mapped file.
          Any write to this channel results in a private copy of the data.
          Of course if this is a new file it will not do a copy on write.
    */
    public static BaseMapper<Reader, AbstractIterator<JSONElement>> readerJsonIter = new BaseMapper<Reader, AbstractIterator<JSONElement>>() {
        public AbstractIterator<JSONElement> apply(Reader r) {
            return new HTJSONIterator(r);
        }
    };

    /*
        This assumes that each character in the file is 1 byte in size.  However a char
    */
    public static BaseMapper<Reader, AbstractIterator<JSONElement>> readerJsonIterOfTwitter = new BaseMapper<Reader, AbstractIterator<JSONElement>>() {
        public AbstractIterator<JSONElement> apply(Reader r) {
            return new HTJSONIterator(r, "twitter_co");
        }
    };

    /*
        Copy a byte array to a char array (8 ->16 bits....we are assuming that the
        byte array contains ascii characters.
    */
    /**
     * File object to Reader
     */
    public static BaseMapper<File, Reader> fileReader = fsInputStream.combine(is2reader);
    /**
     * File to JSONElement iterator.
     */
    public static BaseMapper<File, AbstractIterator<JSONElement>> fsJsonIter = fileReader.combine(readerJsonIter);
    public static BaseMapper<InputStream, AbstractIterator<JSONElement>> isJsonIter = is2reader.combine(readerJsonIter);
    public static BaseMapper<Reader, AbstractIterator<String>> reader2stringiter = new BaseMapper<Reader, AbstractIterator<String>>() {
        public AbstractIterator<String> apply(Reader reader) {
            return new LineReaderIterator(reader);
        }
    };
    public static BaseMapper<InputStream, AbstractIterator<JsonNode>> inputstream2JacksonjsonReader = FileUtil.is2reader.combine(readerJacksonJsonIter);
    public static BaseMapper<File, AbstractIterator<JsonNode>> fsStaxJsonIter = fsInputStream.combine(inputstream2JacksonjsonReader);
    public static BaseMapper<InputStream, AbstractIterator<JVS>> inputstream2JVSjsonReader = FileUtil.is2reader.combine(readerJVSIter);
    private static int counter = 0;
    private static String s_lnCommand = null;
    private static long fCounter = 0;

    public static final File getFormattedFile(File directory, String pattern, Object... args) {
        return new File(directory, Fmt.Sargs(pattern, args));
    }

    public static final boolean deleteAndRecreateDir(File dir) {
        FileUtil.deleteDirectoryContent(dir, true);
        FileUtil.ensureDirectoryExists(dir);
        return true;
    }

    public static boolean fileExistsAndNotEmpty(File file) {
        return file != null && file.length() > 0;
    }

    public static synchronized int getCounter() {
        return counter++;
    }

    public static OutputStream getOutputStreamAtEndOfFile(File f) throws FileNotFoundException {
        return new FileOutputStream(f, true);
    }

    public static File getUniqueTmpFile(String extension) {
        File f;
        do {
            long time = System.currentTimeMillis();
            f = new File(Fmt.S("%s/%s-%s-%s.%s",
                    Env.getNodeId(), Env.getTempDirectory().getAbsolutePath(),
                    Long.toString(time), getCounter(),
                    extension));

        }
        while (f.exists());
        return f;
    }

    public static File getUniqueFileName(File directory, String extension) {
        File f;
        do {
            long time = System.currentTimeMillis();
            f = new File(Fmt.S("%s/%s-%s.%s",
                    directory.getAbsolutePath(),
                    Long.toString(time), getCounter(),
                    extension));

        }
        while (f.exists());
        return f;
    }

    public static void saveStreamToFile(InputStream in, File outFile)
            throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outFile);
            byte[] buf = new byte[4096];
            int bytes_read;
            while ((bytes_read = in.read(buf)) != -1) {
                out.write(buf, 0, bytes_read);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static File[] getOrderedFilesByOrdinal(File directoryPath, String pattern) {
        FilenameFilter filter = new StartsWithFilenameFilter(pattern);
        File[] files = directoryPath.listFiles(filter);
        Arrays.sort(files);
        return files;
    }

    public static void setSingleStringFromFile(File file, String word)
            throws IOException {
        DataOutputStream dos = getDataOutputStreamForFile(file);
        dos.writeUTF(word);
        dos.flush();
        dos.close();
    }

    public static String getSingleStringFromFile(File file)
            throws IOException {
        DataInputStream dis = getDataInputStreamForFile(file);
        String result = dis.readUTF();
        dis.close();
        return result;
    }

    public static ByteBuffer getMemoryMappedFile(File file, boolean forReadWrite) {
        try {
            if (forReadWrite) {
                // writeable buffer.  If this is a
                FileChannel rwChannel = new RandomAccessFile(file, "rw").getChannel();
                return rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, (int) rwChannel.size());

            } else {
                FileChannel roChannel = new RandomAccessFile(file, "r").getChannel();
                return roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) roChannel.size());
            }
        } catch (IOException e) {

        }
        return null;
    }

    public static CharArrayWrapper getCharArrayFromASCIIFile(File file) {
        ByteBuffer bb = getMemoryMappedFile(file, false);
        int remaining = bb.remaining();
        byte cbArray[] = new byte[remaining];
        bb.get(cbArray);
        CharArrayWrapper wrapper = CharArrayWrapper.getWrapper(cbArray.length);
        int size = ArrayUtil.copyByteArrayToCharNormalizing(cbArray, wrapper.getArray());

        // set new size
        wrapper.setSize(size);
        return wrapper;
    }

    public static int copyByteArrayToChar(byte[] array, char[] result) {
        for (int i = 0; i < array.length; i++) {
            result[i] = (char) array[i];
        }
        return result.length;
    }

    /**
     * Delete file if it exists.
     *
     * @param file
     * @return if successfull.  False if file does not exist or it is not deletable.
     */
    public static final boolean deleteIfNotNull(File file) {
        if (FileUtil.nullOrNotExist(file)) {
            return false;
        }
        return file.delete();
    }

    /**
     * Write a listFiles of strings out to a file
     *
     * @param file
     * @param list
     * @return
     * @throws FileNotFoundException
     */
    public static final boolean writeStringListToFile(File file, List<String> list)
            throws FileNotFoundException {
        OutputStream os = FileUtil.getBufferedFileOutputStream(file);
        PrintStream ps = new PrintStream(os);
        for (String s : list) {
            Console.println(ps, s);
        }
        ps.flush();
        ps.close();
        return true;
    }

    /**
     * Write a listFiles of strings out to a file
     *
     * @param file
     * @param msg
     * @return
     * @throws FileNotFoundException
     */
    public static final boolean writeStringToFile(File file, String msg)
            throws FileNotFoundException {
        OutputStream os = FileUtil.getBufferedFileOutputStream(file);
        PrintStream ps = new PrintStream(os);
        Console.println(ps, msg);
        ps.flush();
        ps.close();
        return true;
    }

    /**
     * copy the directory structure.  Does not preserve any file bits.
     *
     * @param inputDir
     * @param outputDir
     * @return
     * @throws java.io.IOException
     */
    public static final boolean copyDirectory(File inputDir, File outputDir) throws IOException {
        File files[] = inputDir.listFiles();
        for (File file : files) {
            File target = new File(outputDir, file.getName());
            if (file.isDirectory()) {
                target.mkdirs();
                copyDirectory(file, target);
            } else {
                copy(file, target);
            }
        }
        return true;
    }

    /**
     * If the file doesnt exist or is not a file then return true.
     *
     * @param file
     * @return
     */
    public static final boolean nullOrNotExistOrNotFile(File file) {
        if (nullOrNotExist(file)) {
            return true;
        }
        return !file.isFile();
    }

    /**
     * determine if the file is null or non existent.
     *
     * @param file
     * @return
     */
    public static final boolean nullOrNotExist(File file) {
        if (file != null) {
            return !file.exists();
        }
        return true;

    }

    public static final boolean nullOrNotExistOrEmpty(File file) {
        if (nullOrNotExist(file)) {
            return false;
        }
        return file.length() == 0;
    }

    /**
     * determine if the file exists.
     *
     * @param file
     * @return
     */
    public static final boolean notNullAndExists(File file) {
        if (file != null) {
            return file.exists();
        }
        return false;

    }

    /**
     * Non atomic file swap. Uses intermediate file name to swap files.
     *
     * @param a
     * @param b
     * @return true if a has become b and be has become a
     */
    public static final boolean swap(File a, File b) {
        File temp = getTempFileWithFromPeerFileWithExtension(a, "tmp");
        try {
            a.renameTo(temp);
        } catch (SecurityException se) {

            return false;
        }
        try {
            b.renameTo(a);
        } catch (SecurityException se) {
            try {
                // try to revert back
                temp.renameTo(a);
                return false;
            } catch (SecurityException se2) {
                return false;
            }
        }

        try {
            temp.renameTo(b);
        } catch (SecurityException se) {
            // should perhaps revert everything.
            return false;
        }
        return true;
    }

    /**
     * Get the extension of a filename, example: File("c:/a/b/c.txt") => "txt" File("c:/a/b/c") => null
     *
     * @param file
     * @return file extension or null if no extension found.
     */
    public static final String getFileExtension(File file) {
        return getFileExtension(file.getName());
    }

    /**
     * Given a file size, rounds up to the nearest 4096 (block size)
     *
     * @param size
     * @return
     */
    public static final long roundLengthToBlockSize(long size) {
        if ((size & 4095) == 0) {
            return size;
        }
        size = size >> 12;
        size++;
        size = size << 12;
        return size;
    }

    /**
     * Get the name of a file without the path and without the extension.
     *
     * @param fileName to return just the name without the extension
     * @return name of file without extension.
     */
    public static final String getFileNameSansExtension(File fileName) {
        return getAbsoluteFileNameSansExtension(fileName.getName());

    }

    /**
     * Get the name of a file without the path and without the extension.
     *
     * @param fileName to return just the name without the extension
     * @return name of file without extension.
     */
    public static final String getAbsoluteFileNameSansExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return fileName;
        }
        return fileName.substring(0, index);
    }

    /**
     * Get the name of a file without the path and without the extension.
     *
     * @param fileName to return just the name without the extension
     * @return name of file without extension.
     */
    public static final String getFileNameSansExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        int slashIndex = fileName.lastIndexOf("/");
        if (slashIndex == -1) {
            slashIndex = fileName.lastIndexOf("\\");
        }
        if (slashIndex == -1) {
            slashIndex = 0;
        } else {
            // move past the slash
            slashIndex++;
        }

        if (index == -1) {
            index = fileName.length();
        }
        if (index < slashIndex) {
            // there is dot in a path and not the filename, therefor the index
            // of the . is invalid
            index = fileName.length();

        }

        return fileName.substring(slashIndex, index);
    }

    /**
     * Get the name of a file with the extension and without the path.
     *
     * @param fileName to return just the name without the extension
     * @return name of file without extension.
     */
    public static final String getFileName(String fileName) {
        int slashIndex = fileName.lastIndexOf("/");
        if (slashIndex == -1) {
            slashIndex = fileName.lastIndexOf("\\");
        }
        if (slashIndex == -1) {
            slashIndex = 0;
        } else {
            // move past the slash
            slashIndex++;
        }

        return fileName.substring(slashIndex, fileName.length());
    }

    /**
     * Get the path of a file without the name or extension.
     *
     * @param fileName to return just the path without name or extension
     * @return file path
     */
    public static final String getFilePath(String fileName) {
        int slashIndex = fileName.lastIndexOf("/");
        if (slashIndex == -1) {
            slashIndex = fileName.lastIndexOf("\\");
        }

        if (slashIndex == -1) {
            slashIndex = 0;
        }

        return fileName.substring(0, slashIndex);
    }

    /**
     * Get the extension of a filename, example: "c:/a/b/c.txt" => "txt" "c:/a/b/c" => null
     *
     * @param fileText
     * @return file extension or null if no extension found.
     */
    public static final String getFileExtension(String fileText) {
        int index = fileText.lastIndexOf(".");
        if (index == -1) {
            return null;
        }
        return fileText.substring(index + 1);
    }

    /**
     * take a filename with potentially an extention and return a file name using the new extension:
     * <p/>
     * getFileNameFromPeer("hello.html", "txt") = "hello.txt" getFileNameFromPeer("hello", "html") = "hello.html"
     *
     * @param peer
     * @param ext
     * @return
     */
    public static final String getFileNameFromPeer(String peer, String ext) {
        int index = peer.lastIndexOf(".");
        if (index != -1) {
            peer = peer.substring(0, index);
        }
        return Fmt.S("%s.%s", peer, ext);
    }

    public static final File getFilePeerWithExtension(File file, String extension) {
        return new File(file.getParent(), Fmt.S("%s.%s", FileUtil.getFileNameSansExtension(file), extension));
    }

    /**
     * Provides a temporary file name given an example file. Basically gets the parent directory and generates a unique
     * file name from it.
     *
     * @param a
     * @param ext
     * @return File that should be unique with a given file extension in the same directory as a
     */
    public static final File getTempFileWithFromPeerFileWithExtension(File a,
                                                                      String ext) {
        return getTempFileWithFromDirectoryExtension(a.getParentFile(), ext);
    }

    public static String getPeerFromName(String name, String extension) {
        int index = name.lastIndexOf(".");
        if (index != -1) {
            name = name.substring(0, index);
        }
        return Fmt.S("%s.%s", name, extension);
    }

    private static synchronized long fileCounter() {
        return fCounter++;
    }

    /**
     * Provides a temporary file name given a directory to put it in.
     *
     * @param directory
     * @param ext
     * @return File that should be unique with a given file extension in the same directory as a
     */
    public static final File getTempFileWithFromDirectoryExtension(
            File directory, String ext) {
        return new File(Fmt.S("%s/%s-%s.%s", directory.getAbsolutePath(),
                fileCounter(), System.currentTimeMillis(), ext));
    }

    public static final List<File> deleteDirectoryContent(File dir,
                                                          boolean includeSelf) {
        List<File> l = new ArrayList<File>();
        deleteDirectoryContent(dir, includeSelf, l);
        return l;
    }

    /**
     * recursively delete files in a directory.
     *
     * @param dir
     * @param includeSelf if set deletes also the directory provided.
     * @return list of files that were not deleteable
     */
    private static final void deleteDirectoryContent(File dir,
                                                     boolean includeSelf, List<File> errorList) {
        File files[] = dir.listFiles();

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    deleteDirectoryContent(file, false);
                }
                if (!file.delete()) {
                    errorList.add(file);
                }
            }
            if (includeSelf) {
                dir.delete();
            }
        }
    }

    /**
     * Buffereed Input Stream from a file.
     *
     * @param file
     * @return CInputStream if we were able to open against a file
     * @throws FileNotFoundException
     */
    public static final InputStream getBufferedFileInputStream(File file)
            throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(file));
    }

    /**
     * Buffereed Output Stream from a file.
     *
     * @param file
     * @return COutputStream if we were able to open against a file
     * @throws FileNotFoundException
     */
    public static final OutputStream getBufferedFileOutputStream(File file)
            throws FileNotFoundException {
        return getBufferedFileOutputStream(file, false);
    }

    /**
     * Buffereed Output Stream from a file.
     *
     * @param file
     * @param append true = append to existing file
     * @return COutputStream if we were able to open against a file
     * @throws FileNotFoundException
     */
    public static final OutputStream getBufferedFileOutputStream(File file,
                                                                 boolean append) throws FileNotFoundException {
        return new BufferedOutputStream(new FileOutputStream(file, append));
    }

    /**
     * Buffereed PrintWriter from a file.
     *
     * @param file
     * @return PrintWriter if we were able to open against a file
     * @throws FileNotFoundException
     */
    public static final PrintWriter getBufferedPrintWriterFromFile(File file) {
        try {
            return getBufferedPrintWriterFromOutputStream(getBufferedFileOutputStream(file));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Buffereed PrintWriter from a file.
     *
     * @param file
     * @return PrintWriter if we were able to open against a file
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     */
    public static final PrintWriter getBufferedPrintWriterFromFile(File file,
                                                                   String encoding) throws UnsupportedEncodingException {
        try {
            return new PrintWriter(file, encoding);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Buffereed PrintWriter from a output stream.
     *
     * @param os
     * @return PrintWriter if we were able to open against a file
     * @throws FileNotFoundException
     */
    public static final PrintWriter getBufferedPrintWriterFromOutputStream(
            OutputStream os) {
        return new PrintWriter(os);
    }

    /**
     * get a reader from a file, if the file does not exist or cannot be opened.
     *
     * @param file
     * @return Reader if able to read file.
     */
    public static final Reader getBufferedReaderFromFile(File file)
            throws FileNotFoundException {
        return new InputStreamReader(getBufferedFileInputStream(file));
    }

    /**
     * get a reader from a file, if the file does not exist or cannot be opened.
     *
     * @param file
     * @return Reader if able to read file.
     * @throws UnsupportedEncodingException
     */
    public static final Reader getBufferedReaderFromFile(File file,
                                                         String encoding) throws FileNotFoundException,
            UnsupportedEncodingException {
        return new InputStreamReader(getBufferedFileInputStream(file), encoding);
    }

    /**
     * Buffered data input stream for the provided file.
     *
     * @param file
     * @return DataInputStream if successfull.
     * @throws FileNotFoundException
     */
    public static final DataInputStream getDataInputStreamForFile(File file)
            throws FileNotFoundException {
        return new DataInputStream(getBufferedFileInputStream(file));
    }

    /**
     * Buffered data output stream for the provided file.
     *
     * @param file
     * @return DataOutputStream if successfull.
     * @throws FileNotFoundException
     */
    public static final DataOutputStream getDataOutputStreamForFile(File file)
            throws FileNotFoundException {
        return new DataOutputStream(getBufferedFileOutputStream(file));
    }

    public static final DataOutputStream getDataOutputStreamForFile(File file, boolean append)
            throws FileNotFoundException {
        return new DataOutputStream(getBufferedFileOutputStream(file, append));
    }

    /**
     * Buffered data output stream for the provided file.
     *
     * @param file
     * @return DataOutputStream if successfull.
     * @throws FileNotFoundException
     */
    public static final DataOutputStream getDataOutputStreamForFileAppend(File file)
            throws FileNotFoundException {
        return new DataOutputStream(getBufferedFileOutputStream(file, true));
    }

    /**
     * Buffered object output stream for the provided file.
     *
     * @param file
     * @return HTBaseObjectOutputStream if successfull.
     * @throws FileNotFoundException
     */
    public static final ObjectOutputStream getObjectOutputStreamForFile(File file)
            throws IOException {
        return new ObjectOutputStream(getBufferedFileOutputStream(file));
    }

    public static final ObjectOutputStream getObjectOutputStreamForByteArray(int initialSize)
            throws IOException {
        return new ObjectOutputStream(getByteArrayOutputStream(initialSize));
    }

    public static final ByteArrayOutputStream getByteArrayOutputStream(int initialSize) {
        return new ByteArrayOutputStream(initialSize);
    }

    public static final ByteArrayInputStream getByteArrayInputStream(byte array[]) {
        return new ByteArrayInputStream(array);
    }

    /**
     * Buffered object input stream for the provided file.
     *
     * @param file
     * @return HTBaseObjectInputStream if successfull.
     * @throws FileNotFoundException
     */
    public static final ObjectInputStream getObjectInputStreamForFile(File file)
            throws IOException {
        return new ObjectInputStream(getBufferedFileInputStream(file));
    }

    /**
     * Get a line iterator (string based) for the provided file
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    public static final Iterator<String> getLineReaderIteratorFromFile(
            File file) throws FileNotFoundException {
        return new LineReaderIterator(getBufferedReaderFromFile(file));
    }

    /**
     * Get a line iterator (string based) for the provided file
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static final Iterator<String> getLineReaderIteratorFromFile(
            File file, String encoding) throws FileNotFoundException,
            UnsupportedEncodingException {
        return new LineReaderIterator(getBufferedReaderFromFile(file, encoding));
    }

    /**
     * Get the contents of a file as a listFiles of strings.
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    public static final List<String> getLinesFromFile(File file)
            throws FileNotFoundException {
        List<String> list = new ArrayList<String>();
        Iterator<String> iter = getLineReaderIteratorFromFile(file);
        while (iter.hasNext()) {
            list.add(iter.next());
        }
        return list;
    }

    /**
     * Read a long value from a file. It is a little brain dead, uses a non optimized way to read from the file.
     *
     * @return long value or throws an io exception of number format exception depending on if the file didnt exist /
     * couldnt be read, or the file was empty or didnt contain valid data.
     */
    public static long getLongValFromFile(File logfile) throws IOException,
            NumberFormatException {
        StringBuilder buff = readFromFileThrowException(logfile);
        boolean parseException = false;
        if (buff == null) {
            // no file
            parseException = true;
        }
        String countString = buff.toString();
        if (StringUtil.nullOrEmptyString(countString)) {
            parseException = true;
        }
        if (parseException) {
            throw new NumberFormatException(StringUtil.strcat(
                    "Attempted to parse long from empty file ", logfile));
        }
        return Long.parseLong(countString);
    }

    /**
     * Not that efficient of a mechanism to get the contents of a file into a string buffer....does not take account of
     * file encoding.
     *
     * @para fileIn
     */
    public static StringBuilder readFromFile(File fileIn) {
        try {
            return readFromFileThrowException(fileIn);
        } catch (IOException e) {
            return null;
        }

    }

    /**
     * Not that efficient of a mechanism to get the contents of a file into a string buffer....does not take account of
     * file encoding.
     *
     * @para fileIn
     */
    public static StringBuilder readFromFile(File fileIn, String lineTerminator) {
        try {
            return readFromFileThrowException(fileIn, lineTerminator);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    public static final StreamTokenizer getStringTokenizerFromFile(File file)
            throws FileNotFoundException {
        Reader reader = getBufferedReaderFromFile(file);
        if (reader == null) {
            return null;
        }
        return new StreamTokenizer(reader);
    }

    /**
     * Tokenize a file using very primitive tokenization which is whatever StreamTokenizer uses by default.
     * <p/>
     * Wraps this in an iterator of strings.
     *
     * @param file
     * @return iterator of strings
     * @throws FileNotFoundException
     * @see StreamTokenizer
     */
    public static final Iterator<String> getIteratorFromFileStreamTokenizer(
            File file) throws FileNotFoundException {
        StreamTokenizer tok = getStringTokenizerFromFile(file);
        if (tok == null) {
            return null;
        }
        return new StreamTokenizerIterator(tok);
    }

    /**
     * Dump the contents of a file into a buffer.
     * <p/>
     * This throws away carriage return line feeds. If you wish to include such then call
     * readFromFileThrowException(File, String) variant and provide the seperator.
     *
     * @param fileIn
     * @return buffer containing the content of the file
     * @throws IOException
     */

    public static StringBuilder readFromFileThrowException(File fileIn)
            throws IOException {
        return readFromFileThrowException(fileIn, null);
    }

    /**
     * Dump the contents of a file into a buffer.
     *
     * @param fileIn
     * @param lineSeperator
     * @return buffer containing the content of the file
     * @throws IOException
     */
    public static StringBuilder readFromFileThrowException(File fileIn,
                                                           String lineSeperator) throws IOException {
        FileInputStream file = new FileInputStream(fileIn);
        InputStreamReader buff = new InputStreamReader(file);
        return readFromReaderThrowException(buff, lineSeperator);
    }

    /**
     * Consume into the FSB the contents of the reader.
     *
     * @param reader
     * @param lineSeperator - optional seperator to preserve line boundaries.
     * @return
     * @throws IOException
     */
    public static StringBuilder readFromReaderThrowException(Reader reader,
                                                             String lineSeperator) throws IOException {
        BufferedReader buff = new BufferedReader(reader);
        StringBuilder strBuffer = new StringBuilder();

        /*
         * char buffer[] = new char[DefaultFileReaderBufferSize];
         *
         * int size = buff.read(buffer, 0, DefaultFileReaderBufferSize); while
         * (size != -1) { strBuffer.append(buffer); size = buff.read(buffer, 0,
         * DefaultFileReaderBufferSize); }
         */

        /*
         * The above method of reading by 1K chars at a time in the same buffer
         * is better than the line-by-line reading method below but it doesn't
         * work in all cases. When the file size large and not a multiple of 1K,
         * the last call to buff.read() returns "-1", even though it reads the
         * characters in the buffer. But there's no way of knowing how many
         * chars were read into the buffer so this method had to be abandoned in
         * favor of less optimal method below.
         *  - SK
         */
        String line;
        while ((line = buff.readLine()) != null) {
            if (lineSeperator != null && strBuffer.length() > 0) {
                strBuffer.append(lineSeperator);
            }
            strBuffer.append(line);
        }

        buff.close();
        return strBuffer;
    }

    /**
     * zip a set of files into a single zip file.
     *
     * @param inputFiles
     * @param outputFile
     * @return true if successfull.
     * @throws IOException
     */
    public static final boolean zipFile(File inputFiles[], File outputFile)
            throws IOException {
        byte[] buf = new byte[1024];

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));

        for (File inputFile : inputFiles) {
            FileInputStream in = new FileInputStream(inputFile);

            out.putNextEntry(new ZipEntry(inputFile.getName()));

            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            out.closeEntry();
            in.close();
        }

        out.close();
        return true;
    }

    /**
     * gzips a single file into a single gzip file.   Returns true if successful.
     *
     * @param inputFile     - file to be gzipped.
     * @param outputFile    - resultant gzip file.
     * @param deleteOnError - delete file if error during creation.
     * @return true if successful
     * @throws IOException
     */
    public static final boolean gzipFile(File inputFile, File outputFile, boolean deleteOnError)
            throws IOException {
        String filePathname = outputFile.getAbsolutePath();
        if (!filePathname.endsWith(".gz")) {
            filePathname = StringUtil.strcat(filePathname, ".gz");
            outputFile = new File(filePathname);
        }
        InputStream is = null;
        OutputStream os = null;


        try {
            is = new BufferedInputStream(new FileInputStream(inputFile));
            os = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(outputFile)));

            IOUtil.copyStream(is, os);
        } catch (IOException ioe) {
            if (deleteOnError && outputFile.exists()) {
                outputFile.delete();
            }
            throw ioe;
        } finally {
            /*   close out the streams, completing the gzip file   */
            if (is != null) {
                is.close();
            }
            if (os != null) {

                os.close();
            }
        }

        return true;
    }

    /**
     * Unzips a zip archive to a destination directory. Zip archive may contain one or more zip entries
     *
     * @param zipFile       zip archive
     * @param destDirectory must be a directory (else IOException is thrown)
     * @throws IOException
     */
    public static final void unzipFile(File zipFile, File destDirectory)
            throws IOException {
        if (!destDirectory.isDirectory()) {
            throw new IOException(
                    "UrlCursorParameter 'destDirectory' must be a directory - path: "
                            + destDirectory.getPath());
        }

        ZipFile inputFile = null;
        try {
            inputFile = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> entries = inputFile.entries();
            ZipEntry entry = null;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                InputStream is = null;
                OutputStream os = null;
                try {
                    is = inputFile.getInputStream(entry);
                    os = getBufferedFileOutputStream(new File(destDirectory,
                            entry.getName()));
                    IOUtil.copyStream(is, os);
                } finally {
                    if (is != null) {
                        is.close();
                    }
                    if (os != null) {
                        os.close();
                    }
                }
            }
        } finally {
            if (inputFile != null) {
                inputFile.close();
            }
        }
    }

    /**
     * Unpacks a GZIP file into the destination directory. The file name of the unpacked file is the same, minus ".gz"
     *
     * @param gzipFile
     * @param destDirectory
     * @param deleteOnError true - delete file if exception occurs
     * @return file if successful
     * @throws IOException
     */
    public static final File gunzipFile(File gzipFile, File destDirectory,
                                        boolean deleteOnError) throws IOException {
        String fileName = gzipFile.getName();
        if (!fileName.endsWith(".gz")) {
            throw new IOException(
                    "File is not a gzip file - suffix must be .gz - file: "
                            + fileName);
        }

        fileName = fileName.substring(0, fileName.length() - ".gz".length());
        File destFile = new File(destDirectory, fileName);

        OutputStream os = null;
        InputStream in = null;

        try {
            os = new BufferedOutputStream(new FileOutputStream(destFile));
            in = new BufferedInputStream(new GZIPInputStream(
                    new FileInputStream(gzipFile)));

            IOUtil.copyStream(in, os);
        } catch (IOException ioe) {
            if (deleteOnError && destFile.exists()) {
                destFile.delete();
            }
            throw ioe;
        } finally {
            if (in != null) {
                in.close();
            }
            if (os != null) {
                os.close();
            }
        }

        return destFile;
    }

    /**
     * Write out a FastStringBuffer to a file.
     *
     * @param file
     * @param buffer
     * @return
     * @throws IOException
     */
    public static final boolean writeToFile(File file, StringBuilder buffer)
            throws IOException {
        return writeToFile(file, buffer.toString());
    }

    public static final boolean writeToFileUsingPrintWriter(File file, StringBuilder buffer, String encoding)
            throws IOException {
        Writer writer = FileUtil.getBufferedPrintWriterFromFile(file, encoding);
        writer.write(buffer.toString());
        writer.flush();
        writer.close();
        return false;
    }

    /**
     * Not the best thing todo. We currently take the bytes from the string and dump them out. We are therefor not very
     * I18N friendly at all.
     *
     * @param file
     * @param buffer
     * @return
     * @throws IOException
     */
    public static final boolean writeToFile(File file, String buffer)
            throws IOException {
        return writeToFile(file, buffer, null);
    }

    public static final boolean writeToFile(File file, String buffer, String encoding)
            throws IOException {
        byte bytes[] = buffer.getBytes();
        return writeToFile(file, bytes, 0, bytes.length);
    }

    /**
     * Write out a byte array to the provided file.
     *
     * @param file
     * @param buffer
     * @param start
     * @param size
     * @return
     * @throws IOException
     */
    public static final boolean writeToFile(File file, byte buffer[],
                                            int start, int size) throws IOException {
        OutputStream os = getBufferedFileOutputStream(file);
        os.write(buffer);
        os.close();
        return true;
    }

    /**
     * write out a file with a long value using the data output stream file format.
     *
     * @param file
     * @param val
     * @return true if able to write the long
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static final boolean writeLongValToFile(File file, long val)
            throws IOException {
        DataOutputStream dos = getDataOutputStreamForFile(file);
        dos.writeLong(val);
        dos.flush();
        dos.close();
        return true;
    }

    /**
     * write out a file with a long value using the data output stream file format.
     *
     * @param file
     * @return true if able to write the long
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static final long readLongValToFile(File file)
            throws IOException {
        DataInputStream dos = getDataInputStreamForFile(file);
        try {
            return dos.readLong();
        } finally {
            dos.close();
        }
    }

    /**
     * write out a file with a long value using the data output stream file format.
     *
     * @param file
     * @return true if able to write the long
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static final long readLongStringValFromFile(File file)
            throws IOException {
        DataInputStream dos = getDataInputStreamForFile(file);
        try {
            String s = dos.readUTF();
            return Long.parseLong(s);
        } finally {
            dos.close();
        }
    }

    public static final long readLongStringValFromFileDefaulting(File file, long defaultVal) {
        if (!file.exists()) {
            return defaultVal;
        }
        StringBuilder sb = FileUtil.readFromFile(file);

        String s = sb.toString();
        return Long.parseLong(s);
    }

    public static final void writeLongStringValFromFile(File file, long l) {
        FileUtil.ensureParentDirectories(file, true);
        PrintWriter pw = FileUtil.getBufferedPrintWriterFromFile(file);
        try {
            pw.write(Long.toString(l));
        } finally {
            pw.flush();
            pw.close();
        }
    }

    /**
     * read a file into a byte buffer of the same size as the buffer.
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static final byte[] getFileAsByteArray(File file)
            throws IOException {
        int length = (int) file.length();
        byte[] buffer = new byte[length];
        InputStream is = getBufferedFileInputStream(file);
        is.read(buffer);
        is.close();
        return buffer;

    }

    /**
     * read a text file into a String object.
     *
     * @param is input stream
     * @return
     * @throws IOException
     */
    public static final String getTextFileAsString(InputStream is)
            throws IOException {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Get basic information why a diffNodeGroupAgainstInstanceList failed or succeeded in plain english.
     *
     * @param a
     * @param b
     * @return
     * @throws IOException
     */
    public static String getBinaryDiffExplanation(File a, File b) throws IOException {
        return FileUtil.BinaryDiffReasons[binaryFileDiff(a, b)];
    }

    /**
     * Works on directories
     */
    public static void delete(String fileName) {
        File file = new File(fileName);
        if (file.isDirectory()) {
            String[] files = file.list();
            for (String name : files) {
                delete(fileName + File.separatorChar + name);
            }
        }
        file.delete();
    }

    /**
     * Works on directories
     */
    public static long getFileSize(String fileName) {
        File file = new File(fileName);
        if (file.isDirectory()) {
            String[] files = file.list();
            long size = 0;
            for (int i = 0; i < files.length; i++) {
                size += getFileSize(fileName + File.separatorChar + files[i]);
            }
            return size;
        }

        return file.length();
    }

    /**
     * Fix a path to have correct file separators.
     *
     * @param filePath filePath expressed in unix terms ('/' as directory separator)
     * @return the path fixed to have this platform's correct separator
     */
    public static String fixSeparators(String filePath) {
        char sep = File.separatorChar;
        if (sep != '/') {
            filePath = filePath.replace('/', sep);
        }

        return filePath;
    }

    /**
     * VERY simple line based differ that will compare two files and determine if the lines test.
     *
     * @param aF
     * @param bF
     * @return 0 if same, 1 if file a does not exist 2 if file b does not exist 3 if file sizes different, 4 if byte
     * level difference
     * @throws IOException
     */
    public static final int lineFileDiffer(File aF,
                                           File bF,
                                           String encoding) throws IOException {
        Iterator<String> a = FileUtil.getLineReaderIteratorFromFile(aF, encoding);
        Iterator<String> b = FileUtil.getLineReaderIteratorFromFile(bF, encoding);
        if (a == null || b == null) {
            return 1;
        }
        while (a.hasNext()) {
            if (b.hasNext()) {
                String aS = a.next();
                String bS = b.next();
                if (!aS.equals(bS)) {
                    return 4;
                }
            } else {
                return 4;
            }
        }

        return 0;
    }

    /**
     * VERY simple binary differ that will compare two files and determine if they are exactly the same or not.
     *
     * @param a
     * @param b
     * @return 0 if same, 1 if file a does not exist 2 if file b does not exist 3 if file sizes different, 4 if byte
     * level difference
     * @throws IOException
     */
    public static final int binaryFileDiff(File a, File b) throws IOException {
        InputStream isA;
        InputStream isB;
        try {
            isA = getBufferedFileInputStream(a);
        } catch (FileNotFoundException fnfe) {
            return 1;
        }
        try {
            isB = getBufferedFileInputStream(b);
        } catch (FileNotFoundException fnfe) {
            return 2;
        }
        if (a.length() != b.length()) {
            return 3;
        }
        if (IOUtil.getStreamsIdentical(isA, isB)) {
            return 0;
        }
        // bytes are different
        return 4;
    }

    /**
     * Returns the location of the temp dir name
     *
     * @return The location of the temp dir name or null if one cannot be found
     * @throws IOException
     */
    public static final String getTempDirName() throws IOException {
        String tempDirName = null;
        File file = File.createTempFile("temp", "temp");
        if (file != null) {
            tempDirName = file.getParent();
            file.delete();
        }
        return tempDirName;
    }

    /**
     * Copy srcFile to destFile. Necessary parent directories for destFile will be created. destFile will be
     * overwritten.
     *
     * @param srcFile
     * @param destFile
     * @throws IOException
     */
    public static void copy(File srcFile, File destFile) throws IOException {
        FileChannel srcChannel = null;
        FileChannel dstChannel = null;
        try {
            // Create channel on the source
            srcChannel = new FileInputStream(srcFile).getChannel();

            // create necessary parent directories
            destFile.getParentFile().mkdirs();
            // Create channel on the destination
            dstChannel = new FileOutputStream(destFile).getChannel();

            // Copy file contents from source to destination
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
        } finally {
            // Close the channels
            if (srcChannel != null) {
                srcChannel.close();
            }
            if (dstChannel != null) {
                dstChannel.close();
            }
        }
    }

    public static final List<File> findFilteredFiles(FilenameFilter filter, File dirFile, boolean recursive) {
        List<File> result = new ArrayList<File>();
        findFilteredFiles(filter, dirFile, recursive, result);
        return result;
    }

    public static final void findFilteredFiles(FilenameFilter filter,
                                               File dirFile,
                                               boolean recursive,
                                               List<File> result) {
        // get the files for this directory
        File[] files = dirFile.listFiles(filter);
        if (files == null) {
            return;
        }
        for (File fi : files) {
            result.add(fi);
        }

        if (recursive) {
            // find all directories and recurse
            FilenameFilter dfilter = new IsDirectoryFilenameFilter();
            File[] dirfiles = dirFile.listFiles(dfilter);
            for (File df : dirfiles) {
                findFilteredFiles(filter, df, true, result);
            }
        }
    }

    /**
     * Move anything with a certain pattern to an output directory.
     *
     * @param filter
     * @param directory
     * @param moveTo
     * @return
     */
    public static final int moveFilteredFiles(FilenameFilter filter,
                                              File directory, File moveTo) {
        File[] list = directory.listFiles(filter);
        for (File f : list) {
            String name = f.getName();
            File moveToFile = new File(moveTo, name);
            f.renameTo(moveToFile);
        }
        return list.length;
    }

    /**
     * Get a file in a directory with the current time in ymd_hmms followed by the pattern provided.
     * <p/>
     * <p/>
     * SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
     *
     * @param directory
     * @param pattern
     * @return
     */
    public static final File getDatedFileFromPattern(File directory,
                                                     String pattern, String extension) {
        Date ct = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HH-mm-SSSS");
        String fileName = Fmt.S("%s-%s.%s", pattern, sdf.format(ct), extension);
        return new File(directory, fileName);
    }

    /**
     * Recursively remove all files in the directory (including itself).
     *
     * @param dir the directory to remove
     * @throws Exception
     */
    public static void removeDirectory(File dir) throws IOException {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    removeDirectory(file);
                } else {
                    if (!file.delete()) {
                        throw new IOException("Cannot delete file "
                                + file.getAbsolutePath());
                    }
                }
            }
            if (!dir.delete()) {
                throw new IOException("Cannot delete directory "
                        + dir.getAbsolutePath());
            }
        }
    }

    /**
     * Create any needed (writeable) parent directories for the given file (file may or may not exist) if they don't
     * exist
     *
     * @param file
     * @param writeable true if you want the parent dirs to be writeable
     * @return true if successful, else false
     */
    public static boolean ensureParentDirectories(File file, boolean writeable) {

        File parent = file.getParentFile();
        if (nullOrNotExist(parent)) {
            parent.mkdirs();
        }
        return (!nullOrNotExist(parent) && parent.isDirectory() && (!writeable || parent
                .canWrite()));
    }

    /**
     * Ensure that a directory exists.
     *
     * @param file
     * @return
     */
    public static boolean ensureDirectoryExists(File file) {
        if (nullOrNotExist(file)) {
            file.mkdirs();
        }

        return true;
    }

    /**
     * Get the amount of all the files in a directory that meet some criteria.
     *
     * @param dir
     * @param filter
     * @return
     */
    public static final long accrueFileCount(File dir, FilenameFilter filter) {
        if (FileUtil.nullOrNotExist(dir) || !dir.isDirectory()) {
            return -1;
        }
        File files[] = dir.listFiles(filter);
        return files.length;
    }

    /**
     * Get the accrued size of all the files in a directory that meet some criteria.
     *
     * @param dir
     * @param filter
     * @return
     */
    public static final long accrueFileSize(File dir, FilenameFilter filter) {
        long size = 0;
        if (FileUtil.nullOrNotExist(dir) || !dir.isDirectory()) {
            return -1;
        }

        File files[] = dir.listFiles(filter);

        for (File file : files) {
            size += file.length();
        }
        return size;
    }

    /**
     * Sorts contents of a directory and returns the array. Does not make a distinction between files and
     * sub-directories. Use IsFileFilenameFilter if only directories are desired
     *
     * @param directory
     * @param filter
     * @param sortCriteria
     * @return null if directory is not a directory, else array of sorted files
     */
    public static final File[] sortFiles(File directory,
                                         Comparator<File> sortCriteria, FilenameFilter filter) {
        File[] unsorted = directory.listFiles(filter);
        if (unsorted == null || unsorted.length == 0) {
            return null;
        }

        Arrays.sort(unsorted, sortCriteria);

        return unsorted;
    }

    /**
     * Chunk up a string into a path, for instance:
     * <p/>
     * abcdefgh with chunk size of 4: abcd/efgh
     *
     * @param s
     * @param chunkSize
     * @return
     */
    public static final String getStringAsDir(String s, int chunkSize) {
        StringBuilder buf = new StringBuilder();
        int size = s.length();
        int chunks = size / chunkSize;
        for (int i = 0; i < chunks; i++) {
            if (buf.length() != 0) {
                buf.append(File.separatorChar);
            }
            int startIndex = i * chunkSize;
            int endIndex = startIndex + chunkSize;
            buf.append(s, startIndex, endIndex);
        }
        int diff = size - (chunks * chunkSize);
        if (diff > 0) {
            buf.append(File.separatorChar);
            int start = chunks * chunkSize;
            int end = start + diff;
            buf.append(s, start, end);
        }
        return buf.toString();
    }

    public static final String idToHexPath(long id) {
        return idToHexPath(id, 2, '/');
    }

    public static final String idToHexPath(long id, int seperatorPer, char seperator) {
        int shift = 4;
        int charsExpected = 64 / 4;
        char[] buf = new char[64];
        int charPos = 64;
        int radix = 1 << shift;
        long mask = radix - 1;
        int realPos = 64;

        do {
            buf[--charPos] = Digits[(int) (id & mask)];
            --realPos;
            if (realPos % seperatorPer == 0) {
                buf[--charPos] = seperator;
            }

            id >>>= shift;
            charsExpected--;
        }
        while (charsExpected != 0);
        return new String(buf, charPos, (64 - charPos));
    }

    /**
     * Returns the path of a file or directory relative to a directory, in native format.
     * <p/>
     * from fmpp.sourceforge.net: fmpp.util.FileUtil.
     *
     * @return The relative path. It never starts with separator char (/ on UN*X).
     * @throws IOException if the two paths has no common parent directory (such as <code>C:\foo.txt</code> and
     *                     <code>D:\foo.txt</code>), or the the paths are malformed.
     */
    public static String getRelativePath(File fromDir, File toFileOrDir) throws IOException {
        char sep = File.separatorChar;
        String ofrom = fromDir.getCanonicalPath();
        String oto = toFileOrDir.getCanonicalPath();

        boolean needSepEndForDirs;
        if (!ofrom.endsWith(File.separator)) {
            ofrom += sep;
            needSepEndForDirs = false;
        } else {
            needSepEndForDirs = true;
        }

        boolean otoEndsWithSep;
        if (!oto.endsWith(File.separator)) {
            oto += sep;
            otoEndsWithSep = false;
        } else {
            otoEndsWithSep = true;
        }

        String from = ofrom.toLowerCase();
        String to = oto.toLowerCase();

        StringBuffer path = new StringBuffer(oto.length());
        int fromln = from.length();

        goback:
        while (true) {
            if (to.regionMatches(0, from, 0, fromln)) {
                File fromf = new File(ofrom.substring(0, needSepEndForDirs ? fromln : fromln - 1));
                File tof = new File(oto.substring(0, needSepEndForDirs ? fromln : fromln - 1));

                if (fromf.equals(tof)) {
                    break goback;
                }
            }
            path.append(".." + sep);
            fromln--;

            while (fromln > 0 && from.charAt(fromln - 1) != sep) {
                fromln--;
            }

            if (fromln == 0) {
                throw new IOException(Fmt.S("%s%s%s%s", "Could not find common parent directory in these paths: ", ofrom, " and ", oto));
            }
        }

        path.append(oto.substring(fromln));
        if (!otoEndsWithSep && path.length() != 0) {
            path.setLength(path.length() - 1);
        }

        return path.toString();
    }

}
