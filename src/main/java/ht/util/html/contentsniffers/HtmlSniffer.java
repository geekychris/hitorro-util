package ht.util.html.contentsniffers;

import ht.util.html.TagFinder;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 5, 2005 Time: 3:44:09 PM
 * <p/>
 * Mug Shot:
 * <p/>
 * <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd"> <!--
 * Application: null --> <!-- Page: ces/CESFrontPage --> <!-- Generated: Fri Jan 05 12:25:09 PST 2007 --> <html> <head>
 * <meta name="generator" content="Tapestry Application Framework, version 4.0"/> <meta http-equiv="Content-Type"
 * content="text/html;charset=UTF-8"/> <title>Welcome to HiTorro</title> </head> <head> <meta http-equiv="Content-Type"
 * content="text/html; charset=utf-8"/>
 * <p/>
 * <title>ces website</title> <link rel="stylesheet" href="../reset-fonts-grids-min.css" type="text/css" media="screen">
 * <style type="text/css" media="screen">
 * <p/>
 * Tactic:
 * <p/>
 * look for first occurence of <!DOCTYPE> <html>  tag, if found we have html content
 */
public class HtmlSniffer implements ContentSniffer {
    public static final String MIME = "text/html";

    public String getMimeType() {
        return MIME;
    }

    public String getTypeFromContent(String content, String believedContentType) {
        TagFinder tf = new TagFinder();
        tf.set(content, 3000);
        boolean foundDT = false;
        while (tf.next()) {
            if (tf.isTagEqualIgnoreCase("!DOCTYPE")) {
                foundDT = true;
                break;
            }
        }
        while (tf.next()) {
            if (tf.isTagEqualIgnoreCase("html")) {
                return MIME;
            }
        }
        return null;
    }
}
