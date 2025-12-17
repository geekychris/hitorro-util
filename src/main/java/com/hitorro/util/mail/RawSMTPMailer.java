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
package com.hitorro.util.mail;

/**
 * Send an email to an smptp server using raw io
 */

import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.net.NetUtil;

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
