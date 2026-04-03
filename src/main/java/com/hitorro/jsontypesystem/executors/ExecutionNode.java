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
package com.hitorro.jsontypesystem.executors;

import com.hitorro.jsontypesystem.Field;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.json.keys.propaccess.IndexedPart;
import com.hitorro.util.json.keys.propaccess.Part;
import com.hitorro.util.json.keys.propaccess.Propaccess;
import com.hitorro.util.json.keys.propaccess.PropaccessError;

import java.util.ArrayList;
import java.util.List;

public class ExecutionNode<E extends ExecutorAction> {
    static Part lang = new Part("lang");
    private List<E> actionsTmp = new ArrayList<>();
    private List<ExecutionRow<E>> execRowsTmp = new ArrayList<>();
    private ExecutorAction actions[];
    private ExecutionRow<E> execRows[];
    private Field f;
    private boolean shell = true;
    private int finalizedCount = -1;
    private boolean fetchLang;


    public ExecutionNode(Field f) {
        this.f = f;
        if (f != null) {
            fetchLang = f.getType().fetchLang();
        }
    }

    public void project(ProjectionContext pc) throws PropaccessError {
        pc.path.setLength(0);
        project(pc, pc.path, false, null);
    }

    protected String getLang(ProjectionContext pc, Propaccess path) {
        path.append(lang);
        try {
            String l = pc.source.getString(path);
            path.pop();
            return l;
        } catch (PropaccessError propaccessError) {
            Log.util.debug("Could not read lang at %s: %s", path, propaccessError.getMessage());
            path.pop();
        }
        return null;
    }

    public void project(ProjectionContext pc, Propaccess pa, boolean isMulti, String lang) throws PropaccessError {
        if (fetchLang) {
            lang = getLang(pc, pa);
        }
        for (ExecutionRow row : execRows) {
            projectRow(pc, row, pa, isMulti, lang);
        }
        if (actions != null) {
            for (ExecutorAction action : actions) {
                action.project(pc, pa, isMulti, lang);
            }
        }
    }

    public void projectRow(ProjectionContext pc, ExecutionRow row, Propaccess pa, boolean isMulti, String lang) throws PropaccessError {
        Part part = row.getPart();
        pa.append(part);
        if (row.isVector()) {
            isMulti = true;
            int size = pc.source.getSize(pa);
            for (int i = 0; i < size; i++) {
                part.setIndex(i);
                row.node.project(pc, pa, isMulti, lang);
            }
        } else {
            row.node.project(pc, pa, isMulti, lang);
        }

        pa.pop();
    }

    public Field getField() {
        return f;
    }

    public boolean getIsShell() {
        return shell;
    }

    public void setNotShell() {
        shell = false;
    }

    public ExecutorAction[] getActions() {
        return actions;
    }

    public ExecutionRow[] getRows() {
        return execRows;
    }

    public void addAction(E action) {
        actionsTmp.add(action);
    }

    public ExecutionRow addField(Field f) {
        ExecutionRow e = new ExecutionRow(f);
        execRowsTmp.add(e);
        return e;
    }


    public int finalizeNode() {
        if (finalizedCount != -1) {
            return finalizedCount;
        }
        pruneRows();

        execRows = execRowsTmp.toArray(new ExecutionRow[execRowsTmp.size()]);
        execRowsTmp = null;
        actions = actionsTmp.toArray(new ExecutorAction[actionsTmp.size()]);
        actionsTmp = null;
        finalizedCount = actions.length + execRows.length;
        return finalizedCount;
    }

    /**
     * Dump the execution plan tree as a readable string for debugging.
     * Shows the field hierarchy with actions at each level.
     */
    public String dump() {
        StringBuilder sb = new StringBuilder();
        dump(sb, 0);
        return sb.toString();
    }

    private void dump(StringBuilder sb, int depth) {
        String indent = "  ".repeat(depth);
        if (f != null) {
            sb.append(indent).append(f.getName());
            if (f.isVector()) sb.append("[]");
            sb.append(" (").append(f.getType().getName()).append(")");
        } else {
            sb.append(indent).append("[root]");
        }
        if (actions != null && actions.length > 0) {
            sb.append(" → ").append(actions.length).append(" action(s)");
            for (ExecutorAction a : actions) {
                sb.append(" [").append(a.getClass().getSimpleName()).append("]");
            }
        }
        sb.append("\n");
        if (execRows != null) {
            for (ExecutionRow row : execRows) {
                if (row.node != null) {
                    row.node.dump(sb, depth + 1);
                }
            }
        }
    }

    private void pruneRows() {
        int i = execRowsTmp.size() - 1;
        while (i >= 0) {
            ExecutionRow<E> row = execRowsTmp.get(i);
            int computed = row.finalizeNode();
            if (computed == 0) {
                // row not needed so prune it.
                execRowsTmp.remove(i);
            }
            i--;
        }
    }
}

class ExecutionRow<E extends ExecutorAction> {
    Field f;
    ExecutionNode<E> node;
    private Part part;
    private boolean isVector;

    public ExecutionRow(Field f) {
        this.f = f;
        if (f.isVector()) {
            part = new IndexedPart(f.getName());
        } else {
            part = new Part(f.getName());
        }
        isVector = f.isVector();
    }

    public boolean isVector() {
        return isVector;
    }

    public Part getPart() {
        if (part.isIndexed()) {
            return part.clone();
        }
        return part;
    }

    public void setNode(ExecutionNode<E> node) {
        this.node = node;
    }

    public String toString() {
        return Fmt.S("ExecRow: %s", f);
    }

    public ExecutionNode<E> getExecNode() {
        return node;
    }

    public int finalizeNode() {
        if (node == null) {
            return 0;
        }
        return node.finalizeNode();
    }
}
