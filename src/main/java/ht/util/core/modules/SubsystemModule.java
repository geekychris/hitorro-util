package ht.util.core.modules;

/**
 * Copyright (c) 2003-2008 HiTorro, Inc.
 * <p/>
 * User: chris Date: Apr 18, 2004 Time: 9:38:22 AM
 * <p/>
 * Description:
 */
public abstract class SubsystemModule {
    private boolean m_valid = true;

    /*
        Initialized this subsystem.
        This method is called by the system when the module is added to the
        subsystem compContext
        @return true if the subsystem initializes successfully
    */
    public abstract boolean init();

    /*
        registered name for this module.  Allows getting access by this name
    */
    public abstract String getName();

    /*
        Allow debugging info to be sent to the log per request of engineer
    */
    public abstract void dumpDebugState();

    /*
        Called if the system is being orderly shutdown.
    */
    public abstract boolean deinit();

    public boolean isValid() {
        return m_valid;
    }
}
