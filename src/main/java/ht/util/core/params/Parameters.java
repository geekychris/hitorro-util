/*
    Copyright (c) 2003 - present HiTorro All rights reserved.

*/

package ht.util.core.params;

import ht.util.core.Log;
import ht.util.core.string.StringUtil;
import ht.util.xml.SimpleDOMNode;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 *
 */
public class Parameters {
    private Map _values;
    private ParameterMeta[] _meta;

    //--------------------------------------------------------------------------

    private Parameters() {
        _values = null;
        _meta = null;
    }

    //--------------------------------------------------------------------------

    /**
     * Parse an XML input for parameters, checking against the parameter metadata. If the parameter metadata is null, we
     * assume that the parameter values are a simple listFiles of name and string values.
     *
     * @param inNode The XML node to parse
     * @return a Map of the parameters, or null on error
     */
    public static Parameters parseParameters(SimpleDOMNode inNode,
                                             ParameterMeta[] pMeta) {
        Parameters ps = new Parameters();
        ps._values = new HashMap(); // unsynchronized apply, allowing nulls
        ps._meta = pMeta;

        // for every parameter in the XML file, try to put it into the result apply
        SimpleDOMNode[] parameters = inNode.getChildren(ParameterMeta.Parameter);
        for (int ii = 0; ii < parameters.length; ii++) {
            String name = parameters[ii].getAttributeString(ParameterMeta.Name);
            if (name == null) {
                Log.util.error("parseParameters parameter missing name");
                return null;
            }
            if (ps._values.containsKey(name)) {
                // this parameter is already known - this must be a duplicate
                Log.util.error("parseParameters duplicate parameter %s", name);
                return null;
            }
            // get the metadata for this parameter
            ParameterMeta cp = null;
            if (ps._meta != null) {
                cp = findParameter(ps._meta, name);
                if (cp == null) {
                    Log.util.error("parseParameters parameter %s  superfluous", name);
                    return null;
                }
            }

            Object pval = parseParameter(parameters[ii], cp);
            ps._values.put(name, pval);
        }

        // for every parameter required in the configuration, make sure it is in the apply
        int plen = (ps._meta == null) ? 0 : ps._meta.length;
        for (int jj = 0; jj < plen; jj++) {
            ParameterMeta oneMeta = ps._meta[jj];
            if (oneMeta.isRequired()) {
                if (!ps._values.containsKey(oneMeta.getName())) {
                    Log.util.error("parseParameters missing required parameter %s", oneMeta.getName());
                    return null;
                }
            }
        }

        // everything is successful
        return ps;
    }

    //--------------------------------------------------------------------------

    /**
     * Parse a single parameter, checking against the parameter metadata.
     *
     * @param cp Metadata about the parameter, if null we assume a scalar string
     * @return an object representing the value of the parameter
     */
    private static Object parseParameter(SimpleDOMNode pnode,
                                         ParameterMeta cp) {
        if (cp == null || cp.getType() == ParameterMeta.TypeString) {
            String value = pnode.getAttributeString(ParameterMeta.Value);
            // note that we allow a null value
            return value;
        } else if (cp.getType() == ParameterMeta.TypeBoolean) {
            boolean val = pnode.getAttributeBoolean(ParameterMeta.Value);
            Boolean value = val ? Boolean.TRUE : Boolean.FALSE;
            return value;
        } else if (cp.getType() == ParameterMeta.TypeInteger) {
            int val = pnode.getAttributeInt(ParameterMeta.Value);
            // bleh - convertToPdf to object
            Integer value = new Integer(val);
            return value;
        } else if (cp.getType() == ParameterMeta.TypeVector) {
            Vector vval = getVectorParameter(pnode, cp);
            return vval;
        } else if (cp.getType() == ParameterMeta.TypeObject) {
            Object val = getObjectParameter(pnode, cp);
            return val;
        } else if (cp.getType() == ParameterMeta.TypeComposite) {
            // just a recursive call
            return parseParameters(pnode, cp.getSubTypes());
        } else {
            Log.util.error("parseParameter unknown type for %s", cp.getType());
            return null;
        }
    }

    //--------------------------------------------------------------------------

    private static Vector getVectorParameter(SimpleDOMNode pnode, ParameterMeta cp) {
        // get the type of the elements
        // and recurse to get the content
        Vector result = new Vector();
        ParameterMeta subType = cp.getSubTypes()[0];
        SimpleDOMNode[] elements = pnode.getChildren(ParameterMeta.Value);
        for (int ii = 0; ii < elements.length; ii++) {
            result.add(parseParameter(elements[ii], subType));
        }

        return result;
    }

    //--------------------------------------------------------------------------

    /**
     * Get the value for an object parameter. We will instantiate an object for the object parameter.  The class we
     * instantiate will be of the class specified in the optional classname attribute of the parameter (in which case we
     * make sure that that class is an instance of the type specified in the parameter metadata) or else we'll
     * instantiate the type from the metadata.  In either case we call the default constructor.
     *
     * @param pnode The parameter data node we're reading
     * @param cp    the metadata for this parameter
     * @return The valid object, or null in the case of trouble
     */
    private static Object getObjectParameter(SimpleDOMNode pnode, ParameterMeta cp) {
        try {
            // get the metadata-specified class
            Class metaClass = Class.forName(cp.getClassName());
            // check for classname attribute in the parameter data
            Class paramClass = null;
            String cname = pnode.getAttributeString(ParameterMeta.ClassName);
            if (cname != null) {
                paramClass = Class.forName(cname);
                // check for assignability
                if (!metaClass.isAssignableFrom(paramClass)) {
                    Log.util.error("getObjectParameter", "cannot assign " + paramClass.getName() + " to " +
                            metaClass.getName() + " for parameter " + cp.getName());
                    return null;
                }

                // it checks out, put paramClass int metaClass for instantiation
                metaClass = paramClass;
            }

            // now instantiate the class
            Object obj = metaClass.newInstance();
            return obj;
        } catch (ClassNotFoundException cnfe) {
            Log.util.error("getObjectParameter", cnfe.toString());
            return null;
        } catch (InstantiationException ie) {
            Log.util.error("getObjectParameter", ie.toString());
            return null;
        } catch (IllegalAccessException iae) {
            Log.util.error("getObjectParameter", iae.toString());
            return null;
        }
    }

    //--------------------------------------------------------------------------

    private static ParameterMeta findParameter(ParameterMeta[] params,
                                               String name) {
        // short listFiles, just linear search the array
        int plen = (params == null) ? 0 : params.length;
        for (int ii = 0; ii < plen; ii++) {
            if (params[ii].nameEquals(name)) {
                return params[ii];
            }
        }

        // we didn't find anything
        return null;
    }

    //--------------------------------------------------------------------------

    public static void main(String[] args) {
        // read and show the meta
        System.out.println("The meta--------------");
        SimpleDOMNode top = SimpleDOMNode.getTopNode(args[0]);
        ParameterMeta[] cps = ParameterMeta.readParameterMetadata(top);
        for (int ii = 0; ii < cps.length; ii++) {
            cps[ii].printMeta(System.out);
        }

        // read and show all the data
        System.out.println("\nThe data--------------");
        top = SimpleDOMNode.getTopNode(args[1]);
        Parameters params = parseParameters(top, cps);
        params.printData(System.out);

        // fetch a couple of parameters
        System.out.println("\nFetch parameter values-----------");
        System.out.println("s1 is " + params.get("/s1", "**fizz"));
        System.out.println("i1 is " + params.getInt("/i1", 333));
        System.out.println("system/logging/b2 is " + params.getBoolean("/system/logging/b2", false));

        return;
    }

    //--------------------------------------------------------------------------

    public void printData(PrintStream out) {
        Iterator keys = _values.keySet().iterator();
        while (keys.hasNext()) {
            String name = (String) keys.next();
            printDataN(out, name, 0);
        }
    }

    //--------------------------------------------------------------------------

    private void printDataN(PrintStream out, String name, int indent) {
        for (int ii = 0; ii < indent; ii++) {
            out.print("    ");
        }

        out.print(name);
        out.print(" ");

        Object val = _values.get(name);

        ParameterMeta pm = findParameter(_meta, name);

        if (pm.getType() == ParameterMeta.TypeVector) {
            out.println();
            printVectorN(out, (Vector) val, indent + 1);
        } else if (pm.getType() == ParameterMeta.TypeComposite) {
            out.println();
            Parameters subps = (Parameters) val;
            Iterator keys = subps._values.keySet().iterator();
            while (keys.hasNext()) {
                String subname = (String) keys.next();
                subps.printDataN(out, subname, indent + 1);
            }
        } else {
            out.println(val);
        }

        return;
    }

    //--------------------------------------------------------------------------

    private void printVectorN(PrintStream out, Vector val, int indent) {
        for (int jj = 0; jj < val.size(); jj++) {
            for (int ii = 0; ii < indent; ii++) {
                out.print("    ");
            }

            Object obj = val.elementAt(jj);
            if (obj instanceof Vector) {
                out.println("()");
                printVectorN(out, (Vector) obj, indent + 1);
            } else {
                out.println(obj);
            }
        }

        return;
    }

    //--------------------------------------------------------------------------

    /**
     * Get a string parameter value
     *
     * @param key The key name, e.g. /system/logging/mainfile
     * @param def The default value
     */
    public String get(String key, String def) {
        String[] keys = toKeys(key, "/");
        Object obj = getObjectForKey(keys);
        ParameterMeta pm = getMetaForKey(keys);

        if (pm.getType() == ParameterMeta.TypeString) {
            return (obj != null) ? (String) obj : def;
        } else {
            return def;
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Get a boolean parameter value
     *
     * @param key The key name, e.g. /system/logging/severity
     * @param def The default value
     */
    public int getInt(String key, int def) {
        String[] keys = toKeys(key, "/");
        Object obj = getObjectForKey(keys);
        ParameterMeta pm = getMetaForKey(keys);

        if (pm.getType() == ParameterMeta.TypeInteger) {
            return (obj != null) ? ((Integer) obj).intValue() : def;
        } else {
            return def;
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Get a boolean parameter value
     *
     * @param key The key name, e.g. /system/logging/on
     * @param def The default value
     */
    public boolean getBoolean(String key, boolean def) {
        String[] keys = toKeys(key, "/");
        Object obj = getObjectForKey(keys);
        ParameterMeta pm = getMetaForKey(keys);

        if (pm.getType() == ParameterMeta.TypeBoolean) {
            return (obj != null) ? ((Boolean) obj).booleanValue() : def;
        } else {
            return def;
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Get a vector parameter value
     *
     * @param key The key name, e.g. /system/logging/categorynames
     * @param def The default value
     */
    public Vector getVector(String key, Vector def) {
        String[] keys = toKeys(key, "/");
        Object obj = getObjectForKey(keys);
        ParameterMeta pm = getMetaForKey(keys);

        if (pm.getType() == ParameterMeta.TypeVector) {
            return (obj != null) ? (Vector) obj : def;
        } else {
            return def;
        }
    }

    //--------------------------------------------------------------------------

    private String[] toKeys(String keyString, String separator) {
        // the key should start with the separator
        // and then we'll look for the separator between tokens
        if (keyString.charAt(0) != separator.charAt(0)) {
            return null;
        }

        return StringUtil.tokenizeFromSingleChar(keyString.substring(1), separator);
    }

    //--------------------------------------------------------------------------

    private Object getObjectForKey(String[] keys) {
        // we're traversing composites until the last element of the key
        Parameters cur = this;
        for (int ii = 0; ii < keys.length - 1; ii++) {
            cur = (Parameters) cur._values.get(keys[ii]);
        }

        // get the leaf value
        return cur._values.get(keys[keys.length - 1]);
    }

    //--------------------------------------------------------------------------

    private ParameterMeta getMetaForKey(String[] keys) {
        // we're traversing composites until the last element of the key
        Parameters cur = this;
        for (int ii = 0; ii < keys.length - 1; ii++) {
            cur = (Parameters) cur._values.get(keys[ii]);
        }

        // return the leaf meta
        return findParameter(cur._meta, keys[keys.length - 1]);
    }
}

/*
   An example of parameter metadata xml file:<br>

   <parameters>
       <parameter type="int" name="i1" required="true"/>
       <parameter type="string" name="s1" required="true"/>
       <parameter type="boolean" name="b1" required="true"/>
       <parameter type="vector" name="v1" required="true">
           <vectorcontent type="string"/>
       </parameter>
       <parameter type="object" name="o1" required="false">
           <objectcontent classname="java.util.Random"/>
       </parameter>
       <parameter type="composite" name="system" required="true">
           <parameter type="string" name="s2" required="false"/>
           <parameter type="composite" name="logging" required="true">
               <parameter type="boolean" name="b2" required="true"/>
           </parameter>
       </parameter>
   </parameters>
*/

/*
  An example of parameter data
*/
