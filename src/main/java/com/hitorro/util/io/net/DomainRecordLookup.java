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
package com.hitorro.util.io.net;

import com.hitorro.util.core.Log;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * DNS Lookup using the DNSContextFactory
 */
public class DomainRecordLookup {

    public static final String RECORD_SOA = "SOA";
    public static final String RECORD_A = "A";
    public static final String RECORD_NS = "NS";
    public static final String RECORD_MX = "MX";
    public static final String RECORD_CNAME = "CNAME";

    public static List<String> lookup(String hostName, String record) {
        List<String> result = new ArrayList();
        try {
            Hashtable env = new Hashtable();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            DirContext ictx = new InitialDirContext(env);
            Attributes attrs = ictx.getAttributes(hostName, new String[]{record});
            Attribute attr = attrs.get(record);

            NamingEnumeration attrEnum = attr.getAll();
            while (attrEnum.hasMoreElements()) {
                result.add(attrEnum.next().toString());
            }
        } catch (NamingException e) {
            Log.util.error("Unable to lookup host %s %e", e, e);
        } catch (NullPointerException e) {
            Log.util.error("Unable to lookup host %s %e", e, e);
        }
        return result;
    }
}