package ht.util.core.iterator.reducers;

import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.BaseReducer;

import java.io.PrintWriter;

public class ToPrintWriter implements BaseReducer<String, Integer> {
    private PrintWriter pw;
    private boolean close;

    public ToPrintWriter(PrintWriter pw, boolean close) {
        this.pw = pw;
        this.close = close;
    }

    @Override
    public Integer reduce(AbstractIterator<String> iter) {
        int counter = 0;
        while (iter.hasNext()) {
            String line = iter.next();
            pw.println(line);
            counter++;
        }
        if (close) {
            pw.flush();
            pw.close();
        }

        return counter;
    }
}
