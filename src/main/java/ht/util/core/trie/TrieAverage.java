package ht.util.core.trie;

/**
 * User: chris
 */
class TrieAverage {
    private int _average;
    private int _number;

    public TrieAverage() {
    }

    public void add(int i) {
        setAverage(getAverage() + i);
        setNumber(getNumber() + 1);
    }

    public int getAverage() {
        return _average;
    }

    public void setAverage(int average) {
        _average = average;
    }

    public int getNumber() {
        return _number;
    }

    public void setNumber(int number) {
        _number = number;
    }
}
