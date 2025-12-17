package ht.util.startupframework;

import ht.util.commandandcontrol.Command;
import ht.util.commandandcontrol.CommandRegistry;
import ht.util.core.ArrayUtil;
import ht.util.core.ListUtil;
import ht.util.core.Log;
import ht.util.core.classes.*;
import ht.util.core.classes.membervaranoconstraints.MethodVarAnoMatches;
import ht.util.core.classes.methodanoconstraints.MethodAnnotationMatches;
import ht.util.core.classes.methodanoconstraints.MethodArgCountMatches;
import ht.util.core.classes.methodanoconstraints.MethodNameMatches;
import ht.util.core.opers.HTPredicate;
import ht.util.core.opers.LogicalAndOperator;
import ht.util.core.opers.LogicalOrOperator;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.BaseMappingProperty;
import ht.util.startupframework.phases.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;


/**
 * Encapsulates a service class allowing us to identify how key information is to be provided.  We allow information to
 * be provided in a variety of forms: - Class level annotation - function naming schemes - tagging by annotation on the
 * field or method level.
 */
public class ServiceWrapper {
    private static final MethodVarAnoMatches serviceConstraint = new MethodVarAnoMatches(ServiceProperty.class);
    private static final MatchClass serviceDefConstraint = new MatchClass(ServiceDefinition.class);

    private static final HTPredicate initConst = new LogicalAndOperator(new LogicalOrOperator(new MethodAnnotationMatches(ServiceInit.class),
            new MethodNameMatches("init", true)), new MethodArgCountMatches(4));
    private static final HTPredicate startConst = new LogicalAndOperator(new LogicalOrOperator(new MethodAnnotationMatches(ServiceStart.class),
            new MethodNameMatches("start", true)), new MethodArgCountMatches(1));
    private static final HTPredicate deinitConst = new LogicalAndOperator(new LogicalOrOperator(new MethodAnnotationMatches(ServiceDeInit.class),
            new MethodNameMatches("deInit", true)), new MethodArgCountMatches(0));

    private static final HTPredicate shortNameConst = new LogicalAndOperator(new LogicalOrOperator(new MethodAnnotationMatches(ShortName.class),
            new MethodNameMatches("getName", true)), new MethodArgCountMatches(0));
    private static final HTPredicate getDependenciesConst = new LogicalAndOperator(new LogicalOrOperator(new MethodAnnotationMatches(ServiceDependencies.class),
            new MethodNameMatches("getDependentService", true)), new MethodArgCountMatches(0));
    private static final HTPredicate registerHooksConst = new LogicalAndOperator(new LogicalOrOperator(new MethodAnnotationMatches(RegisterHooks.class),
            new MethodNameMatches("registerHooks", true)), new MethodArgCountMatches(1));

    private Class clazz;
    private Object o;
    private ServiceDefinition sd;
    private boolean initialized;

    public ServiceWrapper() {

    }

    public int hashCode() {
        return clazz.hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof ServiceWrapper) {
            return ((ServiceWrapper) o).clazz.equals(clazz);
        }
        return false;
    }

    public String toString() {
        return clazz.getCanonicalName();
    }

    public void runIfRunnable() {
        if (o instanceof RunnableService) {
            ((RunnableService) o).run();
        }
    }

    public void addDependencies(List<Class> classes) {
        Field fields[] = clazz.getDeclaredFields();
        for (Field f : fields) {
            Dependency d = f.getAnnotation(Dependency.class);

            if (d != null) {
                try {
                    ServiceWrapper sw = null;
                    String name = d.name();
                    if (!StringUtil.nullOrEmptyString(name)) {
                        sw = ServiceContext.getSC().getServiceByShortname(name);
                    } else if (d.clazz() != Object.class) {
                        if (classes != null) {
                            classes.add(d.clazz());
                        } else {
                            sw = ServiceContext.getSC().getModuleByClass(d.clazz());
                        }
                    }

                    if (classes == null && sw == null) {
                        throw new RuntimeException("Dependency " + name + " not initialized");
                    }
                    if (classes == null) {
                        f.setAccessible(true);
                        f.set(this.o, sw.o);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void addNeededInterfaces(List<Class> neededInterfaces) {
        Class[] neededIntf = getDependentServiceInterfaces();
        if (!ArrayUtil.nullOrEmpty(neededIntf)) {
            for (Class ni : neededIntf) {
                ListUtil.addIfAbsent(neededInterfaces, ni);
            }
        }
    }

    public void addAllCommands() {
        Class cmds[] = this.getDebugCommands();
        if (!ArrayUtil.nullOrEmpty(cmds)) {
            for (Class c : cmds) {
                Command cmd = (Command) ClassUtil.getInstanceSwallowError(c, Command.class);
                if (cmd != null) {
                    // provide the command the service object just in case it needs it (probably a better technique is for the
                    // command to ask for the service when called).
                    cmd.setFromService(o);
                    CommandRegistry.getRegistry().add(cmd);
                }
            }
        }

        //
        Class classCmds[] = this.getDebugCommandClasses();
        for (Class clazz : classCmds) {
            CommandRegistry.getRegistry().addAllFromClass(clazz);
        }

        CommandRegistry.getRegistry().addAllFromObject(o);
    }

    public Class getClazz() {
        return clazz;
    }

    public boolean initServiceWrapper(String cString) {
        Class c = ClassUtil.getClassForName(cString, null);
        if (c == null) {
            new ClassNotFoundException(cString);
        }
        return initServiceWrapper(c);
    }

    public boolean initServiceWrapper(Class c) {
        clazz = c;

        sd = (ServiceDefinition) ClassAnoUtil.getClassLevelAnnotation(c, serviceDefConstraint);
        o = ClassUtil.getInstanceSwallowError(c, null);
        return sd != null && o != null;
    }

    /**
     * instantiation of whatever defined a service.
     *
     * @return
     */
    public Object getServiceObject() {
        return o;
    }

    public Class[] getDependentService() {
        if (sd.generatedServices()) {
            List<Class> l = null;
            MethodAnnotation ma = ClassAnoUtil.getMemberFunction(clazz, getDependenciesConst, null);
            if (ma != null) {
                try {
                    l = (List<Class>) ma.getMethod().invoke(o);
                } catch (IllegalAccessException e) {
                    Log.servicecontext.error("module %s unable to init %s %e", clazz, e, e);
                    return null;
                } catch (InvocationTargetException e) {
                    Log.servicecontext.error("module %s unable to init %s %e", clazz, e, e);
                    return null;
                }
                Class deps[] = new Class[l.size()];
                for (int i = 0; i < l.size(); i++) {
                    deps[i] = l.get(i);
                }
                return deps;
            }
        }
        return sd.dependentService();
    }

    public Class[] getDependentServiceInterfaces() {
        return sd.dependentServiceInterfaces();
    }

    public String[] getDbInitShellScripts() {
        return sd.dbInitShellScripts();
    }

    public Class[] getTypeManagedClasses() {
        return sd.typeManagedClasses();
    }

    public String[] getSchedulerJobPaths() {
        return sd.scheduledJobPath();
    }


    /**
     * initialize this module and also any modules that it depends on. This must be a syncrhonous call
     *
     * @param dbInit         true if we are performing a database loadAnnotation / re-loadAnnotation. This is not normal
     *                       startup but performed during an initdb=true
     * @param upgrading
     * @param currentVersion
     * @param targetVersion  @return null if initialized ok, else an error message
     */
    public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
        if (initialized) {
            return null;
        }
        MethodAnnotation ma = ClassAnoUtil.getMemberFunction(clazz, initConst, null);
        if (ma != null) {
            try {
                String result = (String) ma.getMethod().invoke(o, dbInit, upgrading, currentVersion, targetVersion);
                initialized = true;
                return result;
            } catch (IllegalAccessException e) {
                return Fmt.S("module %s unable to init %s %e", this.clazz, e, e);
            } catch (InvocationTargetException e) {
                return Fmt.S("module %s unable to init %s %e target: %s %e", this.clazz, e, e, e.getTargetException(), e.getTargetException());
            }
        } else {
            Log.servicecontext.warn("Module %s does not have an init method", clazz);
        }
        return null;
    }

    /**
     * register an hooks that should be used by a lower layer.  The pattern is for cases where higher levels implement
     * the required implementation that is specified at a lower tier
     *
     * @param dbInit
     * @return null if initialized ok, else an error message
     */
    public String registerHooks(boolean dbInit) {
        MethodAnnotation ma = ClassAnoUtil.getMemberFunction(clazz, registerHooksConst, null);
        if (ma != null) {
            try {
                return (String) ma.getMethod().invoke(o, dbInit);
            } catch (IllegalAccessException e) {
                return Fmt.S("module %s unable to start %s %e", this.clazz, e, e);
            } catch (InvocationTargetException e) {
                return Fmt.S("module %s unable to start %s %e", this.clazz, e, e);
            }
        }
        return null;
    }

    public String start(boolean dbInit) {
        MethodAnnotation ma = ClassAnoUtil.getMemberFunction(clazz, startConst, null);
        if (ma != null) {
            try {
                String result = (String) ma.getMethod().invoke(o, dbInit);
                return result;
            } catch (IllegalAccessException e) {
                return Fmt.S("module %s unable to start %s %e", this.clazz, e, e);
            } catch (InvocationTargetException e) {
                return Fmt.S("module %s unable to start %s %e", this.clazz, e, e);
            }
        } else {
            Log.servicecontext.warn("Module %s does not have an start method", clazz);
        }
        return null;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public String deInit() {
        initialized = false;
        MethodAnnotation ma = ClassAnoUtil.getMemberFunction(clazz, deinitConst, null);
        if (ma != null) {
            try {
                return (String) ma.getMethod().invoke(o);
            } catch (IllegalAccessException e) {
                return Fmt.S("module %s unable to start %s %e", this.clazz, e, e);
            } catch (InvocationTargetException e) {
                return Fmt.S("module %s unable to start %s %e", this.clazz, e, e);
            }
        } else {
            Log.servicecontext.warn("Module %s does not have an deInit method", clazz);
        }
        return null;
    }

    public BaseMappingProperty[] getRequiredPropertyKeys() {
        List<MemberVarAnnotations> list = new ArrayList();
        ClassAnoUtil.getAllMemberVariable(this.clazz, serviceConstraint, list);
        if (!ListUtil.nullOrEmpty(list)) {
            BaseMappingProperty[] pks = new BaseMappingProperty[list.size()];
            for (int i = 0; i < list.size(); i++) {
                MemberVarAnnotations fa = list.get(i);
                try {
                    Object fv = fa.getValue(o);
                    pks[i] = (BaseMappingProperty) fv;
                } catch (IllegalAccessException e) {
                    Log.servicecontext.fatal("Unable to find field %s for class %s during property fetch", fa, clazz.getCanonicalName());
                }

            }
            return pks;
        }
        return null;
    }

    public String getDescription() {
        return sd.description();
    }

    public String getShortName() {
        if (sd.generatedServices()) {
            MethodAnnotation ma = ClassAnoUtil.getMemberFunction(clazz, shortNameConst, null);
            if (ma != null) {
                try {
                    return (String) ma.getMethod().invoke(o);
                } catch (IllegalAccessException e) {
                    return Fmt.S("module %s unable to start %s %e", this.clazz, e, e);
                } catch (InvocationTargetException e) {
                    return Fmt.S("module %s unable to start %s %e", this.clazz, e, e);
                }
            } else {
                Log.servicecontext.warn("Module %s does not have an start method", clazz);
            }
            return null;

        }
        return sd.shortName();
    }

    public Class[] getDebugCommands() {
        return sd.debugCommands();
    }

    public Class[] getDebugCommandClasses() {
        return sd.debugCommandClasses();
    }

    public String[] getUIDirectories() {
        return sd.uiDirectories();
    }
}
