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
package com.hitorro.util.core;

/**
 *
 */
public class UnixEnums {

    public enum SystemResources {
        RLIMIT_CPU(0, "cpu time per process"),
        RLIMIT_FSIZE(1, "file size"),
        RLIMIT_DATA(2, "data segment size"),
        RLIMIT_STACK(3, "stack size"),
        RLIMIT_CORE(4, "core file size"),
        RLIMIT_AS(5, "address space (resident set size)"),
        RLIMIT_RSS(5, "source compatibility alias"),
        RLIMIT_MEMLOCK(6, "locked-in-memory address space"),
        RLIMIT_NPROC(7, "number of processes"),
        RLIMIT_NOFILE(8, "number of open files"),

        RLIM_NLIMITS(9, "total number of resource limits"),

        _RLIMIT_POSIX_FLAG(0x1000, "Set bit for strict POSIX");

        private int resourceOrd;
        private String description;

        SystemResources(int ordinal, String description) {
            resourceOrd = ordinal;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public int getResourceOrdinal() {
            return resourceOrd;
        }
    }

    /*
     * Possible values of the first parameter to getrlimit()/setrlimit(), to
     * indicate for which resource the operation is being performed.
     */


}

