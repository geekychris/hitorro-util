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
import com.hitorro.jsontypesystem.Group;
import com.hitorro.jsontypesystem.Type;
import com.hitorro.jsontypesystem.TypeVisitor;
import com.hitorro.util.json.keys.propaccess.Propaccess;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ExecutionBuilder<E extends ExecutorAction> implements TypeVisitor<ExecutionBuilder> {
    ExecutionRow row;
    private Map<Field, ExecutionNode> executors = new HashMap<>();
    private ExecutorFactory<E> factory;
    private ExecutionNode<E> root = new ExecutionNode<>(null);
    private ExecutionNode<E> curr = root;
    private Stack<ExecutionNode> execStack = new Stack<>();

    public ExecutionBuilder(ExecutorFactory<E> factory) {
        this.factory = factory;
    }

    public ExecutionNode getExecutor() {
        return root;
    }

    public int finalizeNode() {
        int count = root.finalizeNode();
        executors = null; // no longer needed after finalization
        return count;
    }

    @Override
    public void enterType(final Type type, final Propaccess path) {

    }

    @Override
    public void leaveType(final Type type, final Propaccess path) {

    }

    @Override
    public void enterGroup(final Field field, Group group, final Propaccess path) {
        E action = factory.getNew(field, group, path);
        curr.addAction(action);
    }

    @Override
    public void leaveGroup(final Field field, Group group, final Propaccess path) {

    }

    @Override
    public boolean enterField(final Field field, final Propaccess path) {
        boolean visit = true;
        execStack.push(curr);
        row = curr.addField(field);
        curr = executors.get(field);
        if (curr == null) {
            curr = new ExecutionNode<>(field);
            executors.put(field, curr);
        } else {
            visit = false;
        }
        row.setNode(curr);
        return visit;
    }

    public ExecutionRow getTempRow() {
        return row;
    }

    public ExecutionNode getCurrentNode() {
        return curr;
    }

    @Override
    public void leaveField(final Field type, final Propaccess path) {
        curr.setNotShell();
        curr = execStack.pop();
    }
}
