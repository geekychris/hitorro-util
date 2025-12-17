package ht.util.mail;

import ht.util.core.Constants;
import ht.util.core.KeyValue;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;
import ht.util.io.FileUtil;

import javax.mail.*;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.ParseException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: Jul 13, 2004 Time: 6:44:48 PM
 * <p/>
 * Description:
 */
public class MailDecomposer {
    protected File m_outputDir;
    protected List m_files;
    private File m_mimeFile;
    private int m_counter;
    private Map m_map;

    public MailDecomposer(File mimeFile, File outputDir) {
        m_mimeFile = mimeFile;
        m_outputDir = outputDir;
        m_counter = 1;
        m_files = new Vector();
    }

    public static void pr(String s) {
        System.out.println(s);
    }

    public static void dumpEnvelope(Message m) throws MessagingException {
        pr("This is the message envelope");
        pr("---------------------------");
        Address[] a;
        // FROM
        if ((a = m.getFrom()) != null) {
            for (int j = 0; j < a.length; j++) {
                pr("FROM: " + a[j].toString());
            }
        }

        if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
            for (int j = 0; j < a.length; j++) {
                pr("TO: " + a[j].toString());
            }
        }

        pr("SUBJECT: " + m.getSubject());


        Date d = m.getSentDate();
        pr("SendDate: " +
                (d != null ? d.toString() : "UNKNOWN"));

        // FLAGS
        Flags flags = m.getFlags();
        StringBuffer sb = new StringBuffer();
        Flags.Flag[] sf = flags.getSystemFlags(); // get the system flags

        boolean first = true;
        for (int i = 0; i < sf.length; i++) {
            String s;
            Flags.Flag f = sf[i];
            if (f == Flags.Flag.ANSWERED) {
                s = "\\Answered";
            } else if (f == Flags.Flag.DELETED) {
                s = "\\Deleted";
            } else if (f == Flags.Flag.DRAFT) {
                s = "\\Draft";
            } else if (f == Flags.Flag.FLAGGED) {
                s = "\\Flagged";
            } else if (f == Flags.Flag.RECENT) {
                s = "\\Recent";
            } else if (f == Flags.Flag.SEEN) {
                s = "\\Seen";
            } else {
                continue;    // skip it
            }
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(s);
        }

        String[] uf = flags.getUserFlags(); // get the user flag strings
        for (int i = 0; i < uf.length; i++) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(uf[i]);
        }
        pr("FLAGS: " + sb.toString());

        // X-MAILER
        String[] hdrs = m.getHeader("X-Mailer");
        if (hdrs != null) {
            pr("X-Mailer: " + hdrs[0]);
        } else {
            pr("X-Mailer NOT available");
        }
    }

    public List getFileTranslations() {
        return m_files;
    }

    public Map getMessageHeader() {
        return m_map;
    }

    public boolean unpackMIMEFile() {
        Properties props = System.getProperties();

        // Get a Session object
        m_map = new Hashtable();
        Session session = Session.getInstance(props, null);
        session.setDebug(true);
        try {
            InputStream is = FileUtil.getBufferedFileInputStream(m_mimeFile);

            MimeMessage msg = new MimeMessage(session, is);

            Enumeration enumer = msg.getAllHeaders();
            while (enumer.hasMoreElements()) {
                Header o = (Header) enumer.nextElement();
                m_map.put(o.getName(), o.getValue());
            }
            dumpPart(msg, m_outputDir);
            return true;
        } catch (MessagingException me) {
            System.out.println(me);
        } catch (FileNotFoundException fnfe) {
            System.out.println(fnfe);
        } catch (IOException ioe) {
            System.out.println(ioe);
        } catch (Exception r) {
            System.out.println(r);
        }
        return false;
    }

    private void dumpPart(Part p, File outputDir) throws MessagingException, IOException {
        String ct = p.getContentType();
        try {
            pr("CONTENT-TYPE: " + (new ContentType(ct)).toString());
        } catch (ParseException pex) {
            pr("BAD CONTENT-TYPE: " + ct);
        }
        String filename = p.getFileName();
        if (filename != null) {
            pr("FILENAME: " + filename);
        }

        /*
         * Using isMimeType to determine the content type avoids
         * fetching the actual content data until we need it.
         */
        if (p.isMimeType("text/plain")) {
            //pr("This is plain text");
            //pr("---------------------------");
            writeOutFile(p, "tmp-plaintext.txt");
        } else if (p.isMimeType("multipart/*")) {
            //pr("This is a Multipart");
            //pr("---------------------------");
            Multipart mp = (Multipart) p.getContent();

            int count = mp.getCount();
            for (int i = 0; i < count; i++) {
                dumpPart(mp.getBodyPart(i), outputDir);
            }

        } else if (p.isMimeType("message/rfc822")) {
            //pr("This is a Nested Message");
            //pr("---------------------------");
            dumpPart((Part) p.getContent(), outputDir);

        } else {
            String fileNameInMessage = p.getFileName();
            writeOutFile(p, fileNameInMessage);
            pr("---------------------------");
        }
    }

    private boolean writeOutFile(Part p, String fileName)
            throws MessagingException, IOException {
        InputStream is = p.getInputStream();
        String newFileName;

        if (StringUtil.nullOrEmptyString(fileName)) {
            // no filename provided
            fileName = "<<NO FILENAME>>";
            // XXX Should it really be a txt extension?
            newFileName = Fmt.S("temp-%d.txt", m_counter++);
        } else {
            // must substitute a new file name
            String ext = FileUtil.getFileExtension(fileName);
            if (StringUtil.nullOrEmptyString(ext)) {
                // no ext
                newFileName = Fmt.S("temp-%d", m_counter++);
            } else {
                newFileName = Fmt.S("temp-%d.%s", Constants.getInteger(m_counter++), ext);
            }
        }
        KeyValue pair = new KeyValue(fileName, newFileName);
        this.m_files.add(pair);
        File outputName = new File(StringUtil.strcat(m_outputDir.toString(), "/", newFileName));
        FileUtil.saveStreamToFile(is, outputName);
        return true;
    }


}
