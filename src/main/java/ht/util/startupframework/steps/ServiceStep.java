package ht.util.startupframework.steps;

import ht.util.core.error.ErrorCode;

import java.io.IOException;


/**
 * A ServiceStep is the interface defining a phase of an init or deinit of the service compContext.  Implementors are such
 * things as the hook registration, init, start, deinit
 */
public interface ServiceStep {
    String getPhaseName();

    String getPostStepEvent();

    ErrorCode execute(boolean initDb) throws IOException;
}
