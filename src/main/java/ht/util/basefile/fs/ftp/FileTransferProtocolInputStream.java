package ht.util.basefile.fs.ftp;

import ht.util.io.InputStreamDeligate;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.InputStream;

/**
 * Check at the point of close that the client is still in good shape, else throw an exception
 */
public class FileTransferProtocolInputStream extends InputStreamDeligate {
    private FTPClient client;

    public FileTransferProtocolInputStream(InputStream inputStream, FTPClient client) {
        super(inputStream);
        this.client = client;
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (!client.completePendingCommand()) {
            throw new IOException(client.getReplyString());
        }
    }
}
