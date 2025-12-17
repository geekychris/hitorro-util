package ht.util.startupframework;

import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.commandandcontrol.responsemappings.KeyValuePairMapping;
import ht.util.core.*;
import ht.util.core.classes.ClassUtil;
import ht.util.core.error.ErrorCode;
import ht.util.core.events.LocalEventHub;
import ht.util.core.string.Fmt;
import ht.util.json.keys.BaseMappingProperty;
import ht.util.json.keys.BooleanProperty;
import ht.util.json.keys.PropertyKeyValidationException;
import ht.util.startupframework.steps.*;

import java.io.IOException;
import java.util.*;

/**
 * Sub system initialization. Used to manage a set of subsystems and their dependencies. A little like NT System
 * Services. Ensures that subsystems are initialized in the correct order and that their basic validation requirements
 * are satisfied (Properties).
 * <p/>
 * ServiceContext goes through the following phases:
 * <p/>
 * 1) Define the top level modules you wish to initialize through addModule 2) Validate modules (verifies) 3) Initialize
 * modules in order of dependency (lowest level first) ...time passes 4) deinit modules in reverse order of
 * loadAnnotation
 *
 * @author Chris
 */

public class ServiceContext {
    // message topic sent when server is fully up.
    public static final String ServerUp = Start.EventName;
    public static final BooleanProperty DbInit =
            new BooleanProperty("dbinit",
                    "command line flag to indicate the " +
                            "schema should be created and config data loaded",
                    false);
    public static final BooleanProperty NannyManaged =
            new BooleanProperty("managed.nanny",
                    "Managed through a nanny",
                    false);
    protected static ServiceContext sc;
    public State currentState = State.Stopped;
    public ServiceStep[] startupSteps;
    public ServiceStep[] shutdownSteps;
    private boolean initdb;
    private Map<Class, Object> interfaceMap = new HashMap<Class, Object>();

    private List<Class> neededInterfaces = new ArrayList<Class>();
    private boolean initialized = false;


    private List<ServiceWrapper> swList = new ArrayList();
    private Map<Class, ServiceWrapper> swMap = new HashMap();
    private List<Class> persistedClassesSW = new ArrayList<Class>();

    public ServiceContext() {
        this(new ServiceStep[]{new RegisterHooks(), new RegisterInterfaces(), new InitWithUpgrade(), new InitUIDirs(), new InitDBEvents(), new Start()},
                new ServiceStep[]{new ShutdownServices()});
    }

    public ServiceContext(ServiceStep[] start, ServiceStep[] stop) {
        startupSteps = start;
        shutdownSteps = stop;
    }

    public static void setServiceContext(ServiceContext scIn) {
        sc = scIn;
    }

    public static ServiceContext getSC() {
        if (sc == null) {
            sc = new ServiceContext();
        }
        return sc;
    }

    @CommandDef(command = "env.services", description = "Dump the services registered with the system", resultMapper = KeyValuePairMapping.class)
    public static List<GenericKeyValue> getInitializedServices() {
        List<GenericKeyValue> list = new ArrayList<GenericKeyValue>();
        Collection<ServiceWrapper> col = getSC().swMap.values();
        Iterator<ServiceWrapper> mods = col.iterator();
        while (mods.hasNext()) {
            ServiceWrapper service = mods.next();
            list.add(new GenericKeyValue(service.getShortName(), service.getDescription()));
        }
        return list;
    }

    @CommandDef(command = "dms.dumppersistedclasses", description = "Dump the classes to be persisted in hibernate.")
    public static final List<Class> getPersistedClasses() {
        return getSC().persistedClassesSW;
    }

    /**
     * Validate that all the subsystems got valid
     *
     * @return String containing error text if failed to validate, else empty string.
     */
    public static String validateConfigKeys() {
        StringBuilder buff = new StringBuilder();
        for (ServiceWrapper module : ServiceContext.getSC().getServices()) {
            BaseMappingProperty keys[] = module.getRequiredPropertyKeys();
            if (keys == null) {
                continue;
            }
            for (BaseMappingProperty key : keys) {
                try {
                    key.validate();
                } catch (PropertyKeyValidationException e) {
                    Log.servicecontext.error("Error validating key %s for module %s with error %s",
                            key.getKey(), module.getShortName(), e
                                    .getMessage());
                    Console.bprintln(buff,
                            "Error validating key %s for module %s with error %s",
                            key.getKey(), module.getShortName(), e
                                    .getMessage());
                }
            }
        }
        return buff.toString();
    }

    public static void waitForDeInit() {
        while (ServiceContext.getSC().isInitialized()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    public ServiceWrapper getServiceByShortname(String name) {
        for (ServiceWrapper sw : swList) {
            if (sw.getShortName().equalsIgnoreCase(name)) {
                return sw;
            }
        }
        return null;
    }

    public ServiceWrapper getInitializedServiceByShortname(String name) {
        ServiceWrapper module = getServiceByShortname(name);
        if (module == null || !module.isInitialized()) {
            return null;
        }
        return module;
    }

    public List<ServiceWrapper> getServices() {
        return swList;
    }

    public List<Class> getNeededInterfaces() {
        return neededInterfaces;
    }

    public boolean isServiceInitialized(Class service) {
        return swMap.get(service) != null;
    }

    /**
     * Interface is a short name that is not necessarily implementation specific.  Typically registered in the pre-hook
     * but the interface is defined further down the stack so that a lower tier can refer to a higher tier.
     *
     * @param clazz
     * @param s
     * @return
     */
    public Object registerInterface(Class clazz, Object s) {
        return interfaceMap.put(clazz, s);
    }

    public Object getServiceInterface(Class clazz) {
        return interfaceMap.get(clazz);
    }

    public void setState(State state) {
        currentState = state;
    }

    public Map<Class, ServiceWrapper> getModuleMap() {
        return swMap;
    }

    public boolean addModule(String className) {
        className = className.trim();

        Class clazz = ClassUtil.getClassForName(className, null);
        if (clazz == null) {
            Log.servicecontext.error("Unable to find class for ServiceWrapper %s", className);
            return false;
        }
        ServiceWrapper sw = addModulePrivateSW(clazz);
        if (sw == null) {
            Log.servicecontext.error("Unable to initialize ServiceWrapper %s", className);
            return false;
        }
        return true;
    }

    /**
     * Determine if the module compContext is initialized. A module may of triggered shutdown and therefor we need to know
     * this externally by the main entry point of the caller of ModuleContext.loadAnnotation();
     *
     * @return true if modules are initialized and false if they are not
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Initialize all the modules that are required by this runtime. In this process we identify all other modules that
     * it depends on and initialize them first.
     *
     * @return true if initialization was successful
     */

    public void setInitDb(boolean flag) {
        initdb = flag;
    }

    public String init() throws IOException {
        initdb = DbInit.apply();
        for (ServiceStep ss : startupSteps) {
            ErrorCode ec = ss.execute(initdb);
            if (ec != null) {
                String err = Fmt.S("Unable to complete phase %s %s", ss.getPhaseName(), ec.toString());
                deInit();
                return err;
            }
            LocalEventHub.get().event(ss.getPostStepEvent(), "", null);
        }

        Log.servicecontext.info("Service compContext finished module initialization");
        initialized = true;

        currentState = State.Started;

        return null;
    }

    public String getServerState() {
        return currentState.name();
    }

    public boolean deInit() throws IOException {
        for (ServiceStep ss : shutdownSteps) {
            ErrorCode ec = ss.execute(initdb);
            if (ec != null) {
                Log.servicecontext.error("Unable to complete phase %s ", ss.getPhaseName(), ec.toString());
            }
        }
        initialized = false;
        return true;
    }

    /**
     * Helper function to create a class and put it to the listFiles of classes.
     *
     * @param clazz
     * @return
     */

    private ServiceWrapper addModulePrivateSW(Class clazz) {
        boolean added = false;
        ServiceWrapper existingModule = swMap.get(clazz);
        if (existingModule != null) {
            return existingModule;
        }
        ServiceWrapper sw = new ServiceWrapper();
        if (sw.initServiceWrapper(clazz)) {

            added = true;
            swMap.put(sw.getClazz(), sw);
        } else {
            return null;
        }
        Class cs[] = sw.getTypeManagedClasses();
        if (!ArrayUtil.nullOrEmpty(cs)) {
            for (Class c : cs) {
                ListUtil.addIfAbsent(persistedClassesSW, c);
            }
        }
        sw.addAllCommands();
        sw.addNeededInterfaces(neededInterfaces);
        Class modules[] = sw.getDependentService();

        if (modules != null) {
            for (Class childClassName : modules) {
                addModulePrivateSW(childClassName);
            }
        }
        // get dependencies initialized for those marked with @dependency
        List<Class> dependencies = new ArrayList<>();
        sw.addDependencies(dependencies);
        if (!ListUtil.nullOrEmpty(dependencies)) {
            for (Class childClass : dependencies) {
                addModulePrivateSW(childClass);
            }
        }
        sw.addDependencies(null);
        // put at the end thus providing natural ordering of for init
        if (added) {
            ListUtil.addIfAbsent(swList, sw);
        }

        return sw;
    }

    public Object getInitializedModule(Class clazz) {
        ServiceWrapper module = getInitializedServiceWrapper(clazz);
        if (module == null) {
            return null;
        }

        return module.getServiceObject();
    }

    public ServiceWrapper getInitializedServiceWrapper(Class clazz) {
        ServiceWrapper module = swMap.get(clazz);
        if (module == null) {
            return null;
        }

        if (!module.isInitialized()) {
            return null;
        }

        return module;
    }

    public ServiceWrapper getModuleByClass(Class clazz) {
        return swMap.get(clazz);
    }


    public enum State {
        Stopped, Initializing, LoadingEvents, Started
    }
}
