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
package com.hitorro.util.core.classes;

import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.StringUtil;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClassUtil {

    private static final String ClassExt = ".class";
    private static final int ClassExtLength = ClassExt.length();

    /**
     * Given a class name of the form com/foo/bar/Flop.class generate a canonical form of: com.foo.bar.Flop
     *
     * @param s
     * @return
     */
    public static final String translateClassFilenameToCanonical(String s) {
        if (!s.endsWith(ClassExt)) {
            return null;
        }
        s = StringUtil.replace(s, File.separator, ".");
        s = s.substring(0, s.length() - ClassExtLength);
        return s;
    }

    /**
     * Get an instance of a class swallowing whatever error.
     *
     * @param clazz         the name of the class to instantiate
     * @param requiredSuper if non-null, a class or interface that the class needs to be a subclass of
     * @return the created instance, or null if there was any error
     */
    public static final Object getInstanceSwallowError(String clazz, Class requiredSuper) {
        Class c = getClassForName(clazz, requiredSuper);
        if (c == null) {
            return null;
        }
        return getInstanceSwallowError(c, null);
    }

    public static URL getFileForObject(Object o) {
        return getFileForClass(o.getClass());
    }

    public static URL getFileForClass(Class c) {
        ClassLoader loader = c.getClassLoader();
        String name = getClassNameAsRelativeFile(c);
        return loader.getResource(name);
    }

    public static String getClassNameAsRelativeFile(Class c) {
        return c.getCanonicalName().replace(".", "/") + ".class";
    }

    public static final Object getInstanceSwallowError(Class theClass, Class requiredSuper) {
        return getInstanceSwallowError(theClass, requiredSuper, false);
    }

    /**
     * Get an instance of a class swallowing whatever error.
     *
     * @param theClass      the class to instantiate
     * @param requiredSuper if non-null, a class or interface that the class needs to be a subclass of
     * @return the created instance, or null if there was any error
     */
    public static final Object getInstanceSwallowError(Class theClass, Class requiredSuper, boolean logOutput) {
        try {
            if (requiredSuper != null) {
                // make sure that this class is the right type
                if (!requiredSuper.isAssignableFrom(theClass)) {
                    if (logOutput) {
                        Log.util.error("Incorrect superclass getting class %s with superclass of %s",
                                theClass.getName(), requiredSuper.getName());
                    }
                    return null;
                }
            }
            return theClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            Log.util.error("No default constructor for class %s", theClass.getName());
        } catch (InstantiationException e) {
            Log.util.error("Cannot instantiate class %s: %s", theClass.getName(), e.getMessage());
        } catch (IllegalAccessException e) {
            Log.util.error("Cannot access constructor of class %s: %s", theClass.getName(), e.getMessage());
        } catch (java.lang.reflect.InvocationTargetException e) {
            Log.util.error("Constructor threw exception for class %s: %s", theClass.getName(), e.getCause());
        }
        return null;
    }

    /**
     * get an instance of a class swallowing whatever error.
     *
     * @param clazz
     * @return
     */
    public static final Object getInstanceSwallowError(String clazz) {
        return getInstanceSwallowError(clazz, null);
    }

    /**
     * Safely get a method declared directly in a class.
     *
     * @param clazz The class where we're looking for the method
     * @param name  The name of the method
     * @param args  Arguments to the method.  If null, we are looking for a no-arg method.
     * @return the method, or null if it can't be found or accessed
     */
    public static final Method getDeclaredMethodSwallowError(Class clazz, String name, Class[] args) {
        Method result = null;
        try {
            result = clazz.getDeclaredMethod(name, args);
        } catch (NoSuchMethodException nsme) {
            result = null;
        } catch (SecurityException se) {
            result = null;
        }
        return result;
    }

    public static final Class getClassForName(String classname, Class requiredSuper) {
        return getClassForName(classname, requiredSuper, false);
    }

    /**
     * Safely get a class for a given name and check that it is a subclass of, or implements, another class.
     *
     * @param classname     The classname of the class we're getting
     * @param requiredSuper if non-null, a class or interface that the class needs to be a subclass of
     * @return the Class object if all is well, null otherwise
     */
    public static final Class getClassForName(String classname, Class requiredSuper, boolean logError) {
        if (classname == null) {
            return null;
        }

        try {
            Class cl = Class.forName(classname);
            if (requiredSuper != null) {
                // make sure that this class is the right type
                if (!requiredSuper.isAssignableFrom(cl)) {
                    if (logError) {
                        Log.util.error("Incorrect superclass getting class %s with superclass of %s",
                                classname, requiredSuper.getName());
                    }
                    return null;
                }
            }

            return cl;
        } catch (ClassNotFoundException cnfe) {
            if (logError) {
                Log.util.error("Unable to find class %s with error %s %e", classname, cnfe, cnfe);
            }
            return null;
        }
    }

    /**
     * Get the bare class name, shorn of package qualifications.
     *
     * @param cl The class whose name to fetch.
     * @return the class name, or empty string if the class is null
     */
    public static final String getBareName(Class cl) {
        if (cl == null) {
            return "";
        }

        String cname = cl.getName();
        int dotIndex = cname.lastIndexOf('.');
        if (dotIndex >= 0) {
            cname = cname.substring(dotIndex + 1);
        }
        return cname;
    }

    /**
     * Check whether one class is a subclass (or interface implementation) of another.
     *
     * @param cTest  Class to test
     * @param cSuper The superclass we're checking for, or interface we want cTest to implement
     * @return returns true if cTest is a subclass of cSuper
     */
    public static final boolean isSubClass(Class cTest, Class cSuper) {
        if (cSuper == null || cTest == null) {
            return false;
        }
        return cSuper.isAssignableFrom(cTest);
        /*
        while (cTest != null)
        {
            if (cTest == cSuper)
            {
                return true;
            }
            cTest = cTest.getSuperclass();
        }
        return false;
        */
    }


    public static final List<Class> getClassesNeeded(Class c) {
        Class[] declared = c.getDeclaredClasses();
        Class[] classes = c.getClasses();
        Package p = c.getPackage();
        Annotation[] a = c.getAnnotations();
        return new ArrayList();
    }

    /**
     * Determine if a class is abstract or not
     *
     * @param c
     * @return
     */
    public static final boolean isAbstract(Class c) {
        int i = c.getModifiers();
        return Modifier.isAbstract(i);
    }

    public static final byte[] getSerializedObject(Object object)
            throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream oStream = new ObjectOutputStream(stream);
        oStream.writeObject(object);
        oStream.flush();
        oStream.close();
        stream.close();
        return stream.toByteArray();
    }

    public static final Object getDeserializedObject(byte[] array)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream stream = new ByteArrayInputStream(array);

        ObjectInputStream iStream = new ObjectInputStream(stream);
        Object o = iStream.readObject();
        iStream.close();
        stream.close();
        return o;
    }

    /**
     * Take a single object from an input stream
     *
     * @param is
     * @return The deserialized object or null if an error occured.
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    public static final Object getObjectFromInputStream(InputStream is)
            throws IOException, ClassNotFoundException {
        try {
            ObjectInputStream ois = new ObjectInputStream(is);
            return ois.readObject();
        } catch (EOFException eof) {
            return null;

        }
    }

    /**
     * given a directory, scan it for files and put them to something that looks like a fully qualified classpath.
     *
     * @param dir
     * @param filter
     * @return
     * @throws IOException
     */
    public static final String getExpandedClassPath(StringBuilder builder, File dir, FilenameFilter filter) throws IOException {
        File list[];
        if (filter == null) {
            list = dir.listFiles();
        } else {
            list = dir.listFiles(filter);
        }
        String sep = Env.getPathSeperator();
        for (File f : list) {
            String fs = f.getCanonicalPath();
            if (!StringUtil.nullOrEmptyOrBlankString(fs)) {
                if (builder.length() > 0) {
                    builder.append(sep);
                }
                builder.append(fs);
            }
        }
        return builder.toString();
    }
}
