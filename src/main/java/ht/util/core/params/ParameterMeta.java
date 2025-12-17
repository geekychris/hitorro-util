/*
    Copyright (c) 2003 - present HiTorro All rights reserved.

*/

package ht.util.core.params;

import ht.util.core.Constants;
import ht.util.core.Log;
import ht.util.core.string.Fmt;
import ht.util.xml.SimpleDOMNode;

import java.io.PrintStream;
import java.util.Vector;

/**
 * Instances of ParameterMeta are metadata about parameters. Parameters are described as having a certain type (which
 * can be hierarchically described), as being required or not, and with a default value (for most types). <br>
 * Parameters to a program are typically described in an XML file, and there is a method
 * <code>readParameterMetadata</code> to read in such a file and return a collection of ParameterMeta.  Parameters to a
 * module are typically constructed statically in the class of the module itself.
 * <p/>
 * Actual parameter values are normally contained in an XML file.  The method <code>parseParameters</code> will read
 * such an XML file and validate it against the parameter metadata.
 * <p/>
 */
public class ParameterMeta {
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
    private ParameterMeta[] _subTypes; // type information for subtype

    //--------------------------------------------------------------------------

    /**
     * Construct a simple ParameterMeta.
     *
     * @param name     The parameter's name
     * @param type     The parameter's type, one of this class's constants
     * @param required True if this is a required parameter
     */
    public ParameterMeta(String name, int type, boolean required) {
        if (type != TypeInteger &&
                type != TypeString &&
                type != TypeVector &&
                type != TypeObject &&
                type != TypeComposite &&
                type != TypeBoolean) {
            Log.util.error("constructor", "bad type parameter " + type);
            type = TypeString;
        }

        if (name == null) {
            Log.util.error("constructor", "null name");
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
     * Construct a vector or composite ParameterMeta. We support configuration parameters which are composites of other
     * types, the contained type is described in a subType ParameterMeta instance passed in.  Vectors and composites may
     * be nested arbitrarily deeply.
     *
     * @param name     The parameter's name
     * @param type     The parameter's type, one of this class's constants
     * @param required True if this is a required parameter
     * @param subTypes Type information for the sub elements
     */
    public ParameterMeta(String name, int type,
                         boolean required, ParameterMeta[] subTypes) {
        this(name, type, required);
        _subTypes = subTypes;

        if (type != TypeVector && type != TypeComposite) {
            Log.util.error("composite constructor", "not composite type");
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Construct an object ParameterMeta. Create a parameter type which is a java object.  In addition to the regular
     * information needed for a parameter, we need to know the java class name.  When the parameter is requested, we
     * will instantiate the java class with its default constructor.
     *
     * @param name      The parameter's name
     * @param type      The parameter's type, one of this class's constants
     * @param required  True if this is a required parameter
     * @param className Name of the java class
     */
    public ParameterMeta(String name, int type,
                         boolean required, String className) {
        this(name, type, required);
        _className = className;

        if (type != TypeObject) {
            Log.util.error("object constructor", "not object type");
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Read an XML form of the parameter metadata. This method will read from an XML node and construct an array of
     * configuration parameter metadata.  Call this as an alternative to hand-creating metadata.
     *
     * @param inNode the XML node we will take the metadata from
     * @return an array of ParameterMeta objects, or null in case of error
     */
    public static ParameterMeta[] readParameterMetadata(SimpleDOMNode inNode) {
        // the top-level node should have a bunch of listChildren which are parameters
        // and nothing else
        // error-handling is weak so far

        Vector tempList = new Vector();
        SimpleDOMNode[] parameters = inNode.getChildren(Parameter);
        for (int ii = 0; ii < parameters.length; ii++) {
            ParameterMeta cp = parseOneParameterMetadata(parameters[ii]);
            tempList.addElement(cp);
        }

        // take all the elements out and put them in an array
        ParameterMeta[] result = new ParameterMeta[tempList.size()];
        return (ParameterMeta[]) tempList.toArray(result);
    }

    //--------------------------------------------------------------------------
    private static ParameterMeta parseOneParameterMetadata(SimpleDOMNode pnode) {
        String name = pnode.getAttributeString(Name);
        boolean required = pnode.getAttributeBoolean(Required);
        String typename = pnode.getAttributeString(Type);
        int type = getTypeFromTypeName(typename);

        ParameterMeta cp = null;
        if (type == TypeVector) {
            // get the vector information
            ParameterMeta[] vinfo = parseVectorContent(pnode);
            cp = new ParameterMeta(name, type, required, vinfo);
        } else if (type == TypeComposite) {
            // get the composite information
            ParameterMeta[] cinfo = parseCompositeContent(pnode);
            cp = new ParameterMeta(name, type, required, cinfo);
        } else if (type == TypeObject) {
            // get the object information
            String classname = parseObjectContent(pnode);
            cp = new ParameterMeta(name, type, required, classname);
        } else {
            // simple type
            cp = new ParameterMeta(name, type, required);
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
    private static ParameterMeta[] parseVectorContent(SimpleDOMNode pnode) {
        // we may have one child that is a vector
        // and we recurse
        SimpleDOMNode[] vs = pnode.getChildren(VectorContent);
        if (vs.length != 1) {
            Log.util.debug("vector meta of length ", Constants.getInteger(vs.length));
        }
        String typename = vs[0].getAttributeString(Type);
        int type = getTypeFromTypeName(typename);
        /*
          If we want to support vectors of things other than simple types,
          we'll want to call parseParameter from here, rather than recursing
          directly on parseVectorContent.  We'll need to do something about
          not requiring names...
        */

        ParameterMeta[] result = new ParameterMeta[1];
        if (type != TypeVector) {
            result[0] = new ParameterMeta("_vx", type, false);
        } else {
            // recurse on the vector
            ParameterMeta[] subt = parseVectorContent(vs[0]);
            result[0] = new ParameterMeta("_v", TypeVector, false, subt);
        }

        return result;
    }

    //--------------------------------------------------------------------------
    private static ParameterMeta[] parseCompositeContent(SimpleDOMNode pnode) {
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
    public String getClassName() {
        return _className;
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
    public ParameterMeta[] getSubTypes() {
        return _subTypes;
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

        out.print(Fmt.S("%s %s %s",
                _name,
                Constants.getInteger(_type),
                Constants.getBoolean(_required)));

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
