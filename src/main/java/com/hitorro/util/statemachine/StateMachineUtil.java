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
package com.hitorro.util.statemachine;

import com.hitorro.util.excelaccess.POICSVReader;
import com.hitorro.util.io.csv.csvconsumer.CSVConsumer;
import com.hitorro.util.statemachine.csvconsumers.EdgeConsumer;
import com.hitorro.util.statemachine.csvconsumers.GroupConsumer;
import com.hitorro.util.statemachine.csvconsumers.ParametersConsumer;
import com.hitorro.util.statemachine.csvconsumers.StateConsumer;

import java.io.File;
import java.io.IOException;

/**
 */
public class StateMachineUtil {
    private static final String SheetGroups = "groups";
    private static final String SheetStates = "states";
    private static final String SheetEdges = "edges";
    private static final String SheetParameters = "parameters";


    /**
     * @param file or directory containing the pages.  We expect the following pages:
     *             <p/>
     *             groups - groups that a state can belong to as a way to allow us to categorize the state states -
     *             state or vertex of the graph. statetransitions - edges of the graph
     * @return
     */
    public static MooreStateMachine initStateRegistry(File file) {
        MooreStateMachine registry = new MooreStateMachine();
        init(registry, file);
        return registry;
    }


    /**
     * @param registry of the states
     * @param file     or directory of state diagram
     */
    private static void init(MooreStateMachine registry, File file) {
        initGroups(registry, file);
        initState(registry, file);
        initStateTransitions(registry, file);
        initParameters(registry, file);
    }


    private static boolean initGroups(MooreStateMachine registry, File groupState) {
        boolean result = metaInit(new GroupConsumer(registry), SheetGroups, groupState, true);
        registry.finalizeGroups();
        return result;
    }


    private static boolean metaInit(CSVConsumer consumer, String page, File file, boolean isRequired) {
        POICSVReader reader = new POICSVReader(consumer);
        try {
            boolean foundPage = reader.read(file, page);

            if (!foundPage && isRequired) {
                Log.statemachine.error("Unable to read statemachine file %s, required page %s", file, page);
            }

            return foundPage;
        } catch (IOException ioe) {
            if (isRequired) {
                Log.statemachine.error("Unable to read state file %s for page %s with error %s %e",
                        file, page, ioe, ioe);
                return false;
            }

            return true;
        }
    }


    private static boolean initState(MooreStateMachine registry, File states) {
        return metaInit(new StateConsumer(registry), SheetStates, states, true);
    }


    private static boolean initStateTransitions(MooreStateMachine registry, File stateTransitions) {
        return metaInit(new EdgeConsumer(registry), SheetEdges, stateTransitions, true);
    }


    private static boolean initParameters(MooreStateMachine registry, File state) {
        return metaInit(new ParametersConsumer(registry, state), SheetParameters, state, false);
    }

}
