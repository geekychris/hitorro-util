package ht.util.core.iterator;


import ht.util.core.string.StringTokenIterator;

public class StringTokenizingIterator extends AbstractIterator<String> {
    private StringTokenIterator sti;
    private String curr;
    private String s;

    public StringTokenizingIterator(String s, String sep) {
        this.s = s;
        sti = new StringTokenIterator(s, sep);
        curr = sti.current();
    }

    public String getRemainder() {
        return s.substring(sti.currentStart());
    }

    @Override
    public boolean hasNext() {
        return curr != null;
    }

    @Override
    public String next() {
        String ret = curr;
        curr = sti.next();
        return ret;
    }

    @Override
    public void remove() {

    }
}

