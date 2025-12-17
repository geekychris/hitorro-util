package ht.util.typesystem;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 3, 2008 Time: 6:27:49 AM
 */
public abstract class BaseSessionFactory<S extends BaseSession> {
    protected static BaseSessionFactory factory;

    public static BaseSessionFactory getFactory() {
        return factory;
    }

    public static void setFactory(BaseSessionFactory factoryIn) {
        factory = factoryIn;
    }

    public static final void closeSession(BaseSession closeMe) {
        factory.close(closeMe);
    }

    public final void commitAndCloseSession(BaseSession closeMe) {
        getFactory().commitAndClose(closeMe);
    }

    public abstract int getSessionCount();

    public abstract S getSession();

    public abstract S getCachedDMSSession();

    public abstract S getCachedDMSSession(String key) throws SessionException;

    public abstract S getDMSSession(String key) throws SessionException;

    public abstract void rollbackCloseSession(S closeMe);

    public abstract void close(S closeMe);

    public abstract void disconnectSession(S closeMe);

    public abstract void rollbackClose(S closeMe);


    public abstract void commitAndClose(S closeMe);
}
