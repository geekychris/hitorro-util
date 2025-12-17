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
package com.hitorro.util.osprocessexec;

import com.hitorro.util.core.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Nov 1, 2006 Time: 10:14:29 AM
 * <p/>
 * From core web programming
 */
public class SimpleExec {

    public static int exec(String command, StringBuilder resultBuffer,
                           boolean wait, String... args) throws IOException, InterruptedException {
        StringBuilder err = new StringBuilder();
        int val = exec(command, resultBuffer, err, wait, args);
        if (err.length() > 0) {
            throw new IOException(err.toString());
        }
        return val;
    }

    public static int exec(String command, StringBuilder resultBuffer,
                           StringBuilder errorBuffer,
                           boolean wait, String... args) throws IOException, InterruptedException {
        Log.util.debug("Executing %s'", command);

        // Start running command, returning immediately.
        Process p = null;
        if (args != null && args.length > 0) {
            String a[] = new String[args.length + 1];
            a[0] = command;
            for (int i = 0; i < args.length; i++) {
                a[i + 1] = args[i];
            }
            p = Runtime.getRuntime().exec(a);
        } else {
            p = Runtime.getRuntime().exec(command);
        }

        // Print the output. Since we read until
        // there is no more input, this causes us
        // to wait until the process is completed

        BufferedInputStream buffer = new BufferedInputStream(p
                .getInputStream());
        BufferedInputStream errBuffer = new BufferedInputStream(p
                .getErrorStream());
        BufferedReader commandResult = new BufferedReader(
                new InputStreamReader(buffer));
        BufferedReader commandErrResult = new BufferedReader(
                new InputStreamReader(errBuffer));
        String s = null;

        while ((s = commandResult.readLine()) != null) {
            resultBuffer.append(s);
            resultBuffer.append("\n");
        }
        while ((s = commandErrResult.readLine()) != null) {
            errorBuffer.append(s);
            errorBuffer.append("\n");
        }

        commandResult.close();
        int retVal = p.waitFor();
        return retVal;
        // Ignore read errors; they mean process is done
    }

}

