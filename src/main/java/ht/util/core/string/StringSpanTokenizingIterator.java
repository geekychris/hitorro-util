package ht.util.core.string;

import ht.util.core.iterator.AbstractIterator;


public class StringSpanTokenizingIterator extends AbstractIterator<StringRange> {
    private StringTokenIterator sti;
    private StringRange curr;
    private String s;

    public StringSpanTokenizingIterator(String s, String sep) {
        this.s = s;
        sti = new StringTokenIterator(s, sep);
        curr = new StringRange(s, sti.currentStart(), sti.currentEnd());
    }

    @Override
    public boolean hasNext() {
        return curr != null;
    }

    @Override
    public StringRange next() {
        String c = sti.next();
        if (c == null) {
            return null;
        }
        return new StringRange(s, sti.currentStart(), sti.currentEnd());
    }

    public String getRest() {
        return s.substring(sti.currentStart());
    }

    @Override
    public void remove() {

    }
}

