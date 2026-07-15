/*
 * Copyright (c) 2006-2026 Chris Collins
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

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fluent builder for {@link EmailMessage}. Complements the existing socket-level {@link MailAgent}
 * by giving callers a template-friendly assembly step separate from send.
 *
 * <pre>{@code
 * EmailMessage msg = EmailBuilder.newMessage()
 *     .to("alice@example.com")
 *     .cc("bob@example.com")
 *     .subject("Report ready")
 *     .html("<p>Your report is <b>ready</b>.</p>")
 *     .attach(Attachment.forFile(reportPdf))
 *     .build();
 * }</pre>
 */
public final class EmailBuilder {

    private final List<String> to = new ArrayList<>();
    private final List<String> cc = new ArrayList<>();
    private final List<String> bcc = new ArrayList<>();
    private final List<Attachment> attachments = new ArrayList<>();
    private String subject = "";
    private String body = "";
    private boolean html = false;
    private String from;

    private EmailBuilder() {}

    public static EmailBuilder newMessage() {
        return new EmailBuilder();
    }

    public EmailBuilder from(String address) { this.from = address; return this; }
    public EmailBuilder to(String... addr)   { for (String a : addr) to.add(a); return this; }
    public EmailBuilder cc(String... addr)   { for (String a : addr) cc.add(a); return this; }
    public EmailBuilder bcc(String... addr)  { for (String a : addr) bcc.add(a); return this; }
    public EmailBuilder subject(String s)    { this.subject = s; return this; }

    public EmailBuilder text(String body) {
        if (body == null) throw new IllegalArgumentException("body must not be null");
        this.body = body;
        this.html = false;
        return this;
    }

    public EmailBuilder html(String body) {
        if (body == null) throw new IllegalArgumentException("body must not be null");
        this.body = body;
        this.html = true;
        return this;
    }

    public EmailBuilder attach(Attachment a) {
        attachments.add(a);
        return this;
    }

    public EmailBuilder attachAll(List<Attachment> as) {
        attachments.addAll(as);
        return this;
    }

    public EmailMessage build() {
        if (to.isEmpty()) throw new IllegalStateException("email requires at least one 'to' address");
        if (subject == null || subject.isEmpty()) throw new IllegalStateException("email requires a subject");
        return new EmailMessage(
                List.copyOf(to),
                List.copyOf(cc),
                List.copyOf(bcc),
                subject,
                body,
                html,
                List.copyOf(attachments)
        );
    }

    /** Render the message onto a JavaMail {@link MimeMessage} bound to {@code session}. */
    public MimeMessage toMimeMessage(Session session) throws MessagingException {
        return renderTo(build(), session, from);
    }

    /** Render an already-built {@link EmailMessage} to a MimeMessage. */
    public static MimeMessage renderTo(EmailMessage msg, Session session, String from)
            throws MessagingException {
        MimeMessage mm = new MimeMessage(session);
        if (from != null && !from.isEmpty()) mm.setFrom(new InternetAddress(from));
        for (String a : msg.to())  mm.addRecipient(Message.RecipientType.TO,  new InternetAddress(a));
        for (String a : msg.cc())  mm.addRecipient(Message.RecipientType.CC,  new InternetAddress(a));
        for (String a : msg.bcc()) mm.addRecipient(Message.RecipientType.BCC, new InternetAddress(a));
        mm.setSubject(msg.subject());

        MimeMultipart multipart = new MimeMultipart();
        MimeBodyPart bodyPart = new MimeBodyPart();
        if (msg.html()) bodyPart.setContent(msg.body(), "text/html; charset=utf-8");
        else            bodyPart.setText(msg.body());
        multipart.addBodyPart(bodyPart);

        for (Attachment a : msg.attachments()) {
            MimeBodyPart part = new MimeBodyPart();
            part.setDataHandler(new DataHandler(new AttachmentDataSource(a)));
            part.setFileName(a.getName());
            multipart.addBodyPart(part);
        }

        mm.setContent(multipart);
        return mm;
    }

    /** Snapshot of current recipients for tests / logging. */
    public List<String> currentRecipients() {
        List<String> all = new ArrayList<>(to.size() + cc.size() + bcc.size());
        all.addAll(to); all.addAll(cc); all.addAll(bcc);
        return Collections.unmodifiableList(all);
    }
}
