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
package com.hitorro.util.typesystem;

/**
 * <p/>
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
