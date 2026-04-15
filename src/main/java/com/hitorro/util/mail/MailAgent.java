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

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.params.GlobalProperties;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.json.keys.propaccess.PropaccessError;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

/**
 * <p/>
 * Send email using socket connection to local sendmail process.
 * <p/>
 * To send mail through hosted gmail: * set username to full email address (xyz@HiTorro.net) * set address to full email
 * address * set password * set smtpHost to "smtp.gmail.com"; * set smtpPort to 465 * set ssl to TRUE Then, call
 * sendmail()
 * <p/>
 * Coming Soon => retrieving mail thru POP3 (pop.gmail.com)
 */
public class MailAgent {
    //  Property Key Root
    public static final String MONITOR_PROPERTY_KEY_ROOT = "monitor.email";
    //  Mail Properties
    public static StringProperty UserName = new StringProperty("username", "Email user name", null);
    public static StringProperty Address = new StringProperty("address", "Email address", null);
    public static StringProperty Password = new StringProperty("password", "Email password", null);
    public static StringProperty SmtpHost = new StringProperty("smtp.host", "Email smtp host", null);
    public static StringProperty SmtpPort = new StringProperty("smtp.port", "Email smtp port", null);
    public static BooleanProperty IsSSL = new BooleanProperty("ssl", "Use SSL to connect to host", false);

    //  Mail Exception
    private static final String MSG_MISSING_TARGET_ADDRESS = "Missing Target Email Address";

    //  SSL Socket Factory
    private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

    private String _userName;
    private String _password;
    private String _address;
    private String _smtpHost;
    private String _smtpPort;
    private boolean _isSSL;
    private boolean _auth;

    /**
     * @param userName - Email account username
     * @param password - Email account password
     * @param address  - Email Address from which to send (note: could be different from username)
     * @param smtpHost - SMTP Host
     * @param smtpPort - SMTP Port
     * @param isSSL    - Use SSL ?
     */
    public MailAgent(String userName, String password, String address, String smtpHost, String smtpPort, boolean isSSL, boolean auth) {
        _userName = userName;
        _password = password;
        _address = address;
        _smtpHost = smtpHost;
        _smtpPort = smtpPort;
        _isSSL = isSSL;
        _auth = auth;
    }

    /**
     * Create new MailAgent from Properties
     *
     * @param propertyPath
     * @return MailAgent
     */

    public static MailAgent newInstance(String propertyPath) throws PropaccessError {
        return newInstance(propertyPath, true);
    }

    public static MailAgent newInstance(String propertyPath, boolean auth) throws PropaccessError {
        JsonNode connectInfo = GlobalProperties.getProperties().get(propertyPath);
        return newInstance(connectInfo, auth);
    }

    public static MailAgent newInstance(JsonNode connectInfo, boolean auth) {
        String username = UserName.apply(connectInfo);
        String password = Password.apply(connectInfo);
        String address = Address.apply(connectInfo);
        String smtpHost = SmtpHost.apply(connectInfo);
        String smtpPort = SmtpPort.apply(connectInfo);
        boolean isSSL = IsSSL.apply(connectInfo);
        return new MailAgent(username, password, address, smtpHost, smtpPort, isSSL, auth);
    }

    public static void email(String emailAddress, String title, String message, String emailProperty) {
        if (StringUtil.nullOrEmptyString(emailAddress)) {
            com.hitorro.util.statemachine.Log.workflow.error("No email provided for title %s", title);
            return;
        }
        try {
            MailAgent m = newInstance(emailProperty, true);

            Attachment[] attachment = new Attachment[0];

            m.sendMail(emailAddress, title, message, attachment);
        } catch (MessagingException e) {
            com.hitorro.util.statemachine.Log.workflow.error("Unable to send email to %s %s, %e", emailAddress, e, e);
        } catch (PropaccessError e) {
            com.hitorro.util.statemachine.Log.workflow.error("Unable to send email to %s %s, %e", emailAddress, e, e);
        }
    }

    /**
     * @param toEmail     Array of email addresses to receive the email
     * @param subject     Subject of the email
     * @param message     Message text of the email
     * @param attachments Array of email Attachments
     * @throws MessagingException
     */

    public void sendMail(String toEmail[], String subject, String message, Attachment attachments[]) throws MessagingException {
        if (toEmail.length > 0) {
            Properties props = new Properties();
            //props.put("mail.debug", "true");
            props.put("mail.smtp.host", _smtpHost);
            props.put("mail.smtp.port", _smtpPort);

            // debug
            if (Log.mail.isDebugEnabled()) {
                props.put("mail.debug", "true");
            }
            // Note: SecurityManager is deprecated for removal in future JDK versions
            Authenticator auth = null;
            if (_userName != null) {
                if (_auth) {
                    props.put("mail.smtp.auth", "true");
                    if (_isSSL) {
                        props.put("mail.smtps.auth", "true");
                    }
                } else {
                    props.put("mail.smtp.auth", "false");
                }
                auth = new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(_userName, _password);
                    }
                };
            }

            if (_isSSL) {

                props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
                props.put("mail.smtp.socketFactory.port", _smtpPort);
                props.put("mail.smtp.socketFactory.fallback", "false");
                // for google?  Unsupported record version Unknown-50.49
                //props.put("mail.smtp.starttls.enable", "true");
                //props.put("mail.transport.protocol", "smtps");
                //props.put("mail.smtps.quitwait", "false");
            }

            Session session = Session.getDefaultInstance(props, auth);
            // google fix???
            //session.setProtocolForAddress("rfc822", "smtps");

            //   if (_isSSL)
            //   {
            //       session.setProtocolForAddress("rfc822", "smtps");
            //   }
            //session.setDebug(debug);

            Message msg = new MimeMessage(session);

            //  From
            InternetAddress addressFrom = new InternetAddress(_address);
            msg.setFrom(addressFrom);

            //  To
            InternetAddress[] addressTo = new InternetAddress[toEmail.length];
            for (int i = 0; i < toEmail.length; i++) {
                addressTo[i] = new InternetAddress(toEmail[i]);
            }
            msg.setRecipients(Message.RecipientType.TO, addressTo);

            //  Subject
            msg.setSubject(subject);

            //  Mail Content
            Multipart multipart = new MimeMultipart();

            //  Message Text
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(message);
            multipart.addBodyPart(messageBodyPart);

            if (attachments != null && attachments.length > 0) {
                for (Attachment attachment : attachments) {
                    messageBodyPart = new MimeBodyPart();

                    messageBodyPart.setDataHandler(new DataHandler(new AttachmentDataSource(attachment)));
                    messageBodyPart.setFileName(attachment.getName());

                    multipart.addBodyPart(messageBodyPart);
                }
            }

            //  Add Parts to Message
            msg.setContent(multipart);

            Transport.send(msg);
        } else {
            throw new MessagingException(MSG_MISSING_TARGET_ADDRESS);
        }
    }

    //  Convenience Methods: Single Email
    public void sendMail(String toEmail, String subject, String message) throws MessagingException {
        sendMail(new String[]{toEmail}, subject, message, (Attachment[]) null);
    }

    public void sendMail(String toEmail, String subject, String message, Attachment attachment) throws MessagingException {
        sendMail(new String[]{toEmail}, subject, message, new Attachment[]{attachment});
    }

    public void sendMail(String toEmail, String subject, String message, Attachment attachments[]) throws MessagingException {
        sendMail(new String[]{toEmail}, subject, message, attachments);
    }

    //  Convenience Methods: Multiple emails
    public void sendMail(String toEmail[], String subject, String message) throws MessagingException {
        sendMail(toEmail, subject, message, (Attachment[]) null);
    }

    public void sendMail(String toEmail[], String subject, String message, Attachment attachment) throws MessagingException {
        sendMail(toEmail, subject, message, new Attachment[]{attachment});
    }
}
