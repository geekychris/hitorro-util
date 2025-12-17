package ht.util.io.net;

import ht.util.core.Log;

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