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
package ht.util.xml;


import ht.util.core.Log;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ConfigurationParameter {
    // trivial type flags

    /**
     * the parameter has type integer
     */
    public static final int TypeInteger = 1;

    /**
     * the parameter has type String
     */
    public static final int TypeString = 2;

    /**
     * the parameter has type boolean
     */
    public static final int TypeBoolean = 3;

    /**
     * the parameter has type java object
     */
    public static final int TypeObject = 4;

    /**
     * the parameter has type vector
     */
    public static final int TypeVector = 5;

    /**
     * the parameter has type composite
     */
    public static final int TypeComposite = 6;

    // commonly used attributes
    /**
     * int type
     */
    public static final String TypenameInt = "int";
    /**
     * string type
     */
    public static final String TypenameString = "string";
    /**
     * boolean type
     */
    public static final String TypenameBoolean = "boolean";
    /**
     * vector type
     */
    public static final String TypenameVector = "vector";
    /**
     * object type
     */
    public static final String TypenameObject = "object";
    /**
     * composite type
     */
    public static final String TypenameComposite = "composite";
    /**
     * name of something
     */
    public static final String Name = "name";
    /**
     * value of something
     */
    public static final String Value = "value";
    /**
     * the name of a parameter node
     */
    public static final String Parameter = "parameter";
    /**
     * type attribute
     */
    public static final String Type = "type";
    /**
     * required attribute
     */
    public static final String Required = "required";
    /**
     * name of vector contents node
     */
    public static final String VectorContent = "vectorcontent";
    /**
     * name of object contents node
     */
    public static final String ObjectContent = "objectcontent";
    /**
     * class name attribute
     */
    public static final String ClassName = "classname";

    // instance variables
    private int _type;
    private boolean _required;
    private String _name;
    private String _className; // name of the class, used only for TypeObject
    private ConfigurationParameter[] _subTypes; // type information for subtype

    //--------------------------------------------------------------------------

    /**
     * Construct a simple ConfigurationParameter.
     *
     * @param name     The parameter's name
     * @param type     The parameter's type, one of this class's constants
     * @param required True if this is a required parameter
     */
    public ConfigurationParameter(String name, int type, boolean required) {
        if (type != TypeInteger &&
                type != TypeString &&
                type != TypeVector &&
                type != TypeObject &&
                type != TypeComposite &&
                type != TypeBoolean) {
            Log.util.error("constructor %s bad type parameter %s", this, type);
            type = TypeString;
        }

        if (name == null) {
            Log.util.error("constructor %s null name", this);
            name = "??";
        }

        _type = type;
        _required = required;
        _name = name;
        _className = null;
        _subTypes = null;
    }

    //--------------------------------------------------------------------------

    /**
     * Construct a vector or composite ConfigurationParameter. We support configuration parameters which are composites
     * of other types, the contained type is described in a subType configurationParameter instance passed in.  Vectors
     * and composites may be nested arbitrarily deeply.
     *
     * @param name     The parameter's name
     * @param type     The parameter's type, one of this class's constants
     * @param required True if this is a required parameter
     * @param subTypes Type information for the sub elements
     */
    public ConfigurationParameter(String name, int type,
                                  boolean required, ConfigurationParameter[] subTypes) {
        this(name, type, required);
        _subTypes = subTypes;

        if (type != TypeVector && type != TypeComposite) {
            Log.util.error("composite constructor %s not composite type", this);
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Construct an object ConfigurationParameter. Create a parameter type which is a java object.  In addition to the
     * regular information needed for a parameter, we need to know the java class name.  When the parameter is
     * requested, we will instantiate the java class with its default constructor.
     *
     * @param name      The parameter's name
     * @param type      The parameter's type, one of this class's constants
     * @param required  True if this is a required parameter
     * @param className Name of the java class
     */
    public ConfigurationParameter(String name, int type,
                                  boolean required, String className) {
        this(name, type, required);
        _className = className;

        if (type != TypeObject) {
            Log.util.error("object constructor %s not object type", this);
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Read an XML form of the parameter metadata. This method will read from an XML node and construct an array of
     * configuration parameter metadata.  Call this as an alternative to hand-creating metadata.
     *
     * @param inNode the XML node we will take the metadata from
     * @return an array of ConfigurationParameter objects, or null in case of error
     */
    public static ConfigurationParameter[] readParameterMetadata(SimpleDOMNode inNode) {
        // the top-level node should have a bunch of listChildren which are parameters
        // and nothing else
        // error-handling is weak so far

        Vector tempList = new Vector();
        SimpleDOMNode[] parameters = inNode.getChildren(Parameter);
        for (int ii = 0; ii < parameters.length; ii++) {
            ConfigurationParameter cp = parseOneParameterMetadata(parameters[ii]);
            tempList.addElement(cp);
        }

        // take all the elements out and put them in an array
        ConfigurationParameter[] result = new ConfigurationParameter[tempList.size()];
        return (ConfigurationParameter[]) tempList.toArray(result);
    }

    //--------------------------------------------------------------------------

    private static ConfigurationParameter parseOneParameterMetadata(SimpleDOMNode pnode) {
        String name = pnode.getAttributeString(Name);
        boolean required = pnode.getAttributeBoolean(Required);
        String typename = pnode.getAttributeString(Type);
        int type = getTypeFromTypeName(typename);

        ConfigurationParameter cp = null;
        if (type == TypeVector) {
            // get the vector information
            ConfigurationParameter[] vinfo = parseVectorContent(pnode);
            cp = new ConfigurationParameter(name, type, required, vinfo);
        } else if (type == TypeComposite) {
            // get the composite information
            ConfigurationParameter[] cinfo = parseCompositeContent(pnode);
            cp = new ConfigurationParameter(name, type, required, cinfo);
        } else if (type == TypeObject) {
            // get the object information
            String classname = parseObjectContent(pnode);
            cp = new ConfigurationParameter(name, type, required, classname);
        } else {
            // simple type
            cp = new ConfigurationParameter(name, type, required);
        }

        return cp;
    }

    //--------------------------------------------------------------------------

    private static int getTypeFromTypeName(String typename) {
        if (TypenameInt.equals(typename)) {
            return TypeInteger;
        } else if (TypenameBoolean.equals(typename)) {
            return TypeBoolean;
        } else if (TypenameString.equals(typename)) {
            return TypeString;
        } else if (TypenameVector.equals(typename)) {
            return TypeVector;
        } else if (TypenameObject.equals(typename)) {
            return TypeObject;
        } else if (TypenameComposite.equals(typename)) {
            return TypeComposite;
        }

        // error - just say it's a string
        return TypeString;
    }

    //--------------------------------------------------------------------------

    private static ConfigurationParameter[] parseVectorContent(SimpleDOMNode pnode) {
        // we may have one child that is a vector
        // and we recurse
        SimpleDOMNode[] vs = pnode.getChildren(VectorContent);
        if (vs.length != 1) {
            System.out.println("vector meta of length " + vs.length);
            //return null;
        }
        String typename = vs[0].getAttributeString(Type);
        int type = getTypeFromTypeName(typename);
        /*
          If we want to support vectors of things other than simple types,
          we'll want to call parseParameter from here, rather than recursing
          directly on parseVectorContent.  We'll need to do something about
          not requiring names...
        */

        ConfigurationParameter[] result = new ConfigurationParameter[1];
        if (type != TypeVector) {
            result[0] = new ConfigurationParameter("_vx", type, false);
        } else {
            // recurse on the vector
            ConfigurationParameter[] subt = parseVectorContent(vs[0]);
            result[0] = new ConfigurationParameter("_v", TypeVector, false, subt);
        }

        return result;
    }

    //--------------------------------------------------------------------------

    private static ConfigurationParameter[] parseCompositeContent(SimpleDOMNode pnode) {
        // we're simply recursing on parsing parameters
        return readParameterMetadata(pnode);
    }

    //--------------------------------------------------------------------------

    private static String parseObjectContent(SimpleDOMNode pnode) {
        SimpleDOMNode[] vs = pnode.getChildren(ObjectContent);
        if (vs.length != 1) {
            return null;
        }

        String cname = vs[0].getAttributeString(ClassName);
        return cname;
    }

    //--------------------------------------------------------------------------

    /**
     * Parse an XML input for parameters, checking against the parameter metadata. If the parameter metadata is null, we
     * assume that the parameter values are a simple listFiles of name and string values.
     *
     * @param inNode    The XML node to parse
     * @param paramMeta Metadata about the parameters, can be null
     * @return a Map of the parameters, or null on error
     */
    public static Map parseParameters(SimpleDOMNode inNode,
                                      ConfigurationParameter[] paramMeta) {
        Map retVal = new HashMap(); // unsynchronized apply, allowing nulls

        // for every parameter in the XML file, try to put it into the result apply
        SimpleDOMNode[] parameters = inNode.getChildren(Parameter);
        for (int ii = 0; ii < parameters.length; ii++) {
            String name = parameters[ii].getAttributeString(Name);
            if (name == null) {
                Log.util.error("parseParameters parameter missing name");
                return null;
            }
            if (retVal.containsKey(name)) {
                // this parameter is already known - this must be a duplicate
                Log.util.error("parseParametersduplicate parameter %s", name);
                return null;
            }
            // get the metadata for this parameter
            ConfigurationParameter cp = null;
            if (paramMeta != null) {
                cp = findParameter(paramMeta, name);
                if (cp == null) {
                    Log.util.error("parseParameters parameter %s is superfluous", name);
                    return null;
                }
            }

            Object pval = parseParameter(parameters[ii], cp);
            retVal.put(name, pval);
        }

        // for every parameter required in the configuration, make sure it is in the apply
        int plen = (paramMeta == null) ? 0 : paramMeta.length;
        for (int jj = 0; jj < plen; jj++) {
            if (paramMeta[jj].isRequired()) {
                if (!retVal.containsKey(paramMeta[jj].getName())) {
                    Log.util.error("parseParameters missing required parameter %s", paramMeta[jj].getName());
                    return null;
                }
            }
        }

        // everything is successful
        return retVal;
    }

    //--------------------------------------------------------------------------

    /**
     * Parse a single parameter, checking against the parameter metadata.
     *
     * @param cp Metadata about the parameter, if null we assume a scalar string
     * @return an object representing the value of the parameter
     */
    private static Object parseParameter(SimpleDOMNode pnode,
                                         ConfigurationParameter cp) {
        if (cp == null || cp.getType() == TypeString) {
            String value = pnode.getAttributeString(Value);
            // note that we allow a null value
            return value;
        } else if (cp.getType() == TypeBoolean) {
            boolean val = pnode.getAttributeBoolean(Value);
            Boolean value = val ? Boolean.TRUE : Boolean.FALSE;
            return value;
        } else if (cp.getType() == TypeInteger) {
            int val = pnode.getAttributeInt(Value);
            // bleh - convertToPdf to object
            Integer value = new Integer(val);
            return value;
        } else if (cp.getType() == TypeVector) {
            Vector vval = getVectorParameter(pnode, cp);
            return vval;
        } else if (cp.getType() == TypeObject) {
            Object val = getObjectParameter(pnode, cp);
            return val;
        } else if (cp.getType() == TypeComposite) {
            // just a recursive call
            return parseParameters(pnode, cp._subTypes);
        } else {
            Log.util.error("parseParameter unknown type for %s", cp.getType());
            return null;
        }
    }

    //--------------------------------------------------------------------------

    private static Vector getVectorParameter(SimpleDOMNode pnode, ConfigurationParameter cp) {
        // get the type of the elements
        // and recurse to get the content
        Vector result = new Vector();
        ConfigurationParameter subType = cp._subTypes[0];
        SimpleDOMNode[] elements = pnode.getChildren(Value);
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
    private static Object getObjectParameter(SimpleDOMNode pnode, ConfigurationParameter cp) {
        try {
            // get the metadata-specified class
            Class metaClass = Class.forName(cp._className);
            // check for classname attribute in the parameter data
            Class paramClass = null;
            String cname = pnode.getAttributeString(ClassName);
            if (cname != null) {
                paramClass = Class.forName(cname);
                // check for assignability
                if (!metaClass.isAssignableFrom(paramClass)) {
                    Log.util.error("null", "getObjectParameter", "cannot assign " + paramClass.getName() + " to " +
                            metaClass.getName() + " for parameter " + cp._name);
                    return null;
                }

                // it checks out, put paramClass int metaClass for instantiation
                metaClass = paramClass;
            }

            // now instantiate the class
            Object obj = metaClass.newInstance();
            return obj;
        } catch (ClassNotFoundException cnfe) {
            Log.util.error("null", "getObjectParameter", cnfe.toString());
            return null;
        } catch (InstantiationException ie) {
            Log.util.error("null", "getObjectParameter", ie.toString());
            return null;
        } catch (IllegalAccessException iae) {
            Log.util.error("null", "getObjectParameter", iae.toString());
            return null;
        }
    }

    //--------------------------------------------------------------------------

    private static ConfigurationParameter findParameter(ConfigurationParameter[] params,
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
        ConfigurationParameter[] cps = readParameterMetadata(top);
        for (int ii = 0; ii < cps.length; ii++) {
            cps[ii].printMeta(System.out);
        }

        // read and show the data
        System.out.println("\nThe data--------------");
        top = SimpleDOMNode.getTopNode(args[1]);
        Map vals = parseParameters(top, cps);
        for (int ii = 0; ii < cps.length; ii++) {
            // if we have a value for the parameter, print it
            Object val = vals.get(cps[ii]._name);
            if (val != null) {
                cps[ii].printData(System.out, val);
            }
        }
    }

    //--------------------------------------------------------------------------

    public String getName() {
        return _name;
    }

    //--------------------------------------------------------------------------

    public boolean nameEquals(String test) {
        return _name.equals(test);
    }

    //--------------------------------------------------------------------------

    public int getType() {
        return _type;
    }

    //--------------------------------------------------------------------------

    public boolean isRequired() {
        return _required;
    }

    //--------------------------------------------------------------------------

    public void printMeta(PrintStream out) {
        printMetaN(out, 0);
    }

    //--------------------------------------------------------------------------

    private void printMetaN(PrintStream out, int indent) {
        for (int ii = 0; ii < indent; ii++) {
            out.print("    ");
        }

        out.print(_name + " " + _type + " " + _required);
        if (_type == TypeVector || _type == TypeComposite) {
            out.println();
            for (int jj = 0; jj < _subTypes.length; jj++) {
                _subTypes[jj].printMetaN(out, indent + 1);
            }
        } else if (_type == TypeObject) {
            out.println(" " + _className);
        } else {
            out.println();
        }
    }

    //--------------------------------------------------------------------------

    public void printData(PrintStream out, Object val) {
        printDataN(out, val, 0);
    }

    //--------------------------------------------------------------------------

    private void printDataN(PrintStream out, Object val, int indent) {
        for (int ii = 0; ii < indent; ii++) {
            out.print("    ");
        }

        out.print(_name);
        out.print(" ");

        if (_type == TypeVector) {
            out.println();
            Vector vv = (Vector) val;
            for (int jj = 0; jj < vv.size(); jj++) {
                _subTypes[0].printDataN(out, vv.elementAt(jj), indent + 1);
            }
        } else if (_type == TypeComposite) {
            out.println();
            Map mm = (Map) val;
            for (int jj = 0; jj < _subTypes.length; jj++) {
                Object subval = mm.get(_subTypes[jj]._name);
                if (subval != null) {
                    _subTypes[jj].printDataN(out, subval, indent + 1);
                }
            }
        } else {
            out.println(val);
        }

        return;
    }
}


