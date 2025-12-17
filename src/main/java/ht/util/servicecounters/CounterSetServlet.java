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