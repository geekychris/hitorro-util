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
package ht.util.servicecounters;

import ht.util.core.ArrayUtil;

import jakarta.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 *
 */
public class CounterSetServlet extends HttpServlet {
    /**
     *
     */
    private static final long serialVersionUID = 4793110215999990139L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Map<String, String[]> params = request.getParameterMap();
        String bits[] = params.get("columns");
        if (ArrayUtil.nullOrEmpty(bits)) {
            return;
        }
        String args = bits[0];
        // write our content
        PrintWriter out = response.getWriter();
        CounterContext ccontext = CounterContext.getContext();
        ColumnSet cs = ccontext.getColumnSet(args);
        cs.renderHeader(out);
        PrintWriter p = new PrintWriter(System.out);
        cs.renderHeader(new PrintWriter(System.out));
        CounterClock cc = CounterService.getService().getClock();
        Object notifier = cc.getNotifier();
        while (true) {
            try {
                synchronized (notifier) {
                    notifier.wait();
                }
            } catch (InterruptedException e) {

            }
            cs.renderRow(out);
            out.flush();
            cs.renderRow(p);
            p.flush();
        }
    }
}