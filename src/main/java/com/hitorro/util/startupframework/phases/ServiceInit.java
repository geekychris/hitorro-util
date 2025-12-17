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
package com.hitorro.util.startupframework.phases;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Initialize this module and also any modules that it depends on. This must be a syncrhonous call. Can be called either
 * via using the function "init" or referring to the method with this annotation.
 * <p/>
 * public String init (boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion)
 *
 * @param dbInit         true if we are performing a database loadAnnotation / re-loadAnnotation. This is not normal
 *                       startup but performed during an initdb=true
 * @param upgrading
 * @param currentVersion
 * @param targetVersion  @return null if initialized ok, else an error message
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceInit {
}
