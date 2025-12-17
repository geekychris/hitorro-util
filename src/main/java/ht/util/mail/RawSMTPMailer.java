package ht.util.mail;

/**
 * Send an email to an smptp server using raw io
 */

import ht.util.core.Log;
import ht.util.core.string.Fmt;
import ht.util.io.net.NetUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class RawSMTPMailer {
    private static final String HEADER_SUBJECT = "Subject: ";
    private static final String EOL = "\r\n";
    private static final String FOOTER = ".\r\n" + "QUIT\r\n";
    private static final String DataFormatString = "%s%s" + HEADER_SUBJECT + "%s" + EOL + EOL + "%s" + EOL + FOOTER;

    /**
     * @return Email header
     */


    public static void sendMessageRawSMTP(String adminEmail, String subject, String message, String from) {
        HashMap host = NetUtil.getHostNameAndAddress();
        String addr = (String) host.get(NetUtil.HOST_HASH_ADDRESS);
        sendMessageRawSMTP(adminEmail, subject, message, addr, from, "localhost", 25);
    }

    public static void sendMessageRawSMTP(String adminEmail, String subject,
                                          String message, String localAddr,
                                          String from, String server, int port) {


        Socket socket = null;
        String lineToBeSent;
        BufferedReader input;
        StringReader stringRead;
        PrintWriter output;


        String helo = Fmt.S("HELO %s \r\n", localAddr);

        String data = Fmt.S(DataFormatString, helo, getHeader(adminEmail, from), subject, message);
        // connect to server
        try {
            socket = new Socket(server, port);
        } catch (UnknownHostException e) {
            Log.util.error("Error %s %e", e, e);
        } catch (IOException e) {
            Log.util.error("Error %s %e", e, e);
        } catch (Exception e) {
            Log.util.error("Error %s %e", e, e);
        }

        if (socket != null) {
            try {
                stringRead = new StringReader(data);
                input = new BufferedReader(stringRead);
                output = new PrintWriter(socket.getOutputStream(), true);

                // get user input and transmit it to server
                lineToBeSent = input.readLine();
                while (lineToBeSent != null) {
                    output.println(lineToBeSent);
                    lineToBeSent = input.readLine();
                }
            } catch (Exception e) {
                Log.util.error("Error %s %e", e, e);
            }

            try {
                socket.close();
            } catch (Exception e) {
                Log.util.error("Error %s %e", e, e);
            }
        }

    }

    private static String getHeader(String adminEmail, String from) {
        return Fmt.S("MAIL FROM: %s \r\n RCPT TO: %s \r\n DATA \r\n", from, adminEmail);
    }
}
