package ht.util.core.longword;

import ht.util.core.hash.FPHash64;
import ht.util.core.string.Fmt;

/**
 * Manipulate a bit of a long
 */
public class NamedBitsOfLong {
    public static final long AllOn = 0xFFFFFFFFFFFFFFFFl;
    private int m_bitOffset;
    private long m_bits;
    private int m_width = 1;
    private long m_mask;
    private String m_name;
    private BiDirectionalKeyLongMap valueMapper;
    private WordBits wordBits;

    public NamedBitsOfLong() {

    }

    public NamedBitsOfLong(WordBits wordBits, String name, int position, int width, BiDirectionalKeyLongMap valueMapper) {
        setBitOffset(name, position, width, wordBits);
        this.valueMapper = valueMapper;
    }

    public NamedBitsOfLong(WordBits wordBits, String name, int position, int width) {
        setBitOffset(name, position, width, wordBits);
    }

    public String mapFromLong(long l) {
        return valueMapper.mapToString(get(l));
    }

    public long getLongHash() {
        String v = Fmt.S("%s.%s.%s", m_name, m_bitOffset, m_width);
        return FPHash64.getFP(v);
    }

    public int getWidth() {
        return m_width;
    }

    public void setBitOffset(String name, int position, int width, WordBits wordBits) {
        m_name = name.toLowerCase();
        m_bitOffset = position;
        m_width = width;
        this.wordBits = wordBits;
        if (m_width == 1) {
            m_bits = 1 << position;

        } else {
            long bits = 0;
            long bit = 0;
            for (int i = 0; i < m_width; i++) {
                int pos = position + i;
                bit = 1l << pos;
                bits = bits | bit;
            }
            m_bits = bits;
        }
        m_mask = AllOn ^ m_bits;
    }

    public String getName() {
        return m_name;
    }

    public int getBitOffset() {
        return m_bitOffset;
    }

    public long set(long val) {
        return val | m_bits;
    }

    public long set(long word, String number) {
        return set(word, valueMapper.mapFromString(number));
    }

    public boolean hasValueMapper() {
        return valueMapper != null;
    }

    public long set(long word, long number) {
        return word = (word & m_mask) | (number << m_bitOffset) & m_bits;
    }

    public long get(long word) {
        return (word & m_bits) >> m_bitOffset;
    }

    public long clear(long val) {
        return val & m_mask;
    }

    public boolean isSet(long val) {
        return (val & m_bits) != 0;
    }

    public void dumpKeyValueToStringBuilder(StringBuilder b, long l) {
        b.append(m_name);
        b.append("=");
        if (m_width == 1) {
            if (isSet(l)) {
                b.append("1");
            } else {
                b.append("0");
            }
        } else {
            long v = get(l);
            b.append(v);
        }
    }
}
