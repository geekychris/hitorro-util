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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EmailBuilder + EmailTemplate")
class EmailBuilderTest {

    private final Session session = Session.getInstance(new Properties());

    @Test
    @DisplayName("build() populates recipients, subject and body")
    void buildBasics() {
        EmailMessage m = EmailBuilder.newMessage()
                .to("a@x.com", "b@x.com")
                .cc("c@x.com")
                .bcc("d@x.com")
                .subject("Hello")
                .text("Body")
                .build();
        assertThat(m.to()).containsExactly("a@x.com", "b@x.com");
        assertThat(m.cc()).containsExactly("c@x.com");
        assertThat(m.bcc()).containsExactly("d@x.com");
        assertThat(m.subject()).isEqualTo("Hello");
        assertThat(m.body()).isEqualTo("Body");
        assertThat(m.html()).isFalse();
        assertThat(m.attachments()).isEmpty();
    }

    @Test
    @DisplayName("html() marks message as HTML")
    void htmlFlag() {
        EmailMessage m = EmailBuilder.newMessage()
                .to("a@x.com").subject("s").html("<b>hi</b>").build();
        assertThat(m.html()).isTrue();
    }

    @Test
    @DisplayName("build() rejects missing recipients and subject")
    void validation() {
        assertThatThrownBy(() -> EmailBuilder.newMessage().subject("s").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("'to'");
        assertThatThrownBy(() -> EmailBuilder.newMessage().to("a@x.com").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("subject");
    }

    @Test
    @DisplayName("toMimeMessage renders recipients, subject and multipart body")
    void renderMime() throws Exception {
        MimeMessage mm = EmailBuilder.newMessage()
                .from("sender@x.com")
                .to("a@x.com", "b@x.com")
                .cc("c@x.com")
                .subject("Hi")
                .text("Hello there")
                .toMimeMessage(session);

        assertThat(mm.getFrom()[0].toString()).isEqualTo("sender@x.com");
        assertThat(mm.getRecipients(Message.RecipientType.TO)).hasSize(2);
        assertThat(mm.getRecipients(Message.RecipientType.CC)).hasSize(1);
        assertThat(mm.getSubject()).isEqualTo("Hi");
        assertThat(mm.getContent()).isInstanceOf(MimeMultipart.class);
    }

    @Test
    @DisplayName("attachments appear as additional multipart parts")
    void withAttachment() throws Exception {
        byte[] payload = "hello".getBytes();
        Attachment a = new Attachment("hello.txt", payload);
        MimeMessage mm = EmailBuilder.newMessage()
                .from("sender@x.com")
                .to("a@x.com")
                .subject("s")
                .text("body")
                .attach(a)
                .toMimeMessage(session);
        MimeMultipart mp = (MimeMultipart) mm.getContent();
        assertThat(mp.getCount()).isEqualTo(2); // body + 1 attachment
        assertThat(mp.getBodyPart(1).getFileName()).isEqualTo("hello.txt");
    }

    @Test
    @DisplayName("EmailTemplate substitutes {{key}} placeholders")
    void templateBasics() {
        String out = EmailTemplate.render("Hello {{name}}, order {{id}} shipped.",
                Map.of("name", "Chris", "id", 42));
        assertThat(out).isEqualTo("Hello Chris, order 42 shipped.");
    }

    @Test
    @DisplayName("EmailTemplate leaves unknown placeholders in place")
    void templateMissingKey() {
        String out = EmailTemplate.render("Hi {{name}} — {{missing}}",
                Map.of("name", "C"));
        assertThat(out).isEqualTo("Hi C — {{missing}}");
    }

    @Test
    @DisplayName("EmailTemplate tolerates whitespace inside braces")
    void templateWhitespace() {
        String out = EmailTemplate.render("{{ name }}", Map.of("name", "C"));
        assertThat(out).isEqualTo("C");
    }
}
