package ht.util.cluster;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
public interface ClusterServiceInterface {
    boolean canRunIfDBLeader();

    boolean getAmINamedSingleton(String name);
}
