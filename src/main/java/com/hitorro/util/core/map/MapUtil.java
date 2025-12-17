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
package com.hitorro.util.core.map;


import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TIntLongIterator;
import gnu.trove.iterator.TLongIntIterator;
import gnu.trove.iterator.TLongLongIterator;
import gnu.trove.map.hash.*;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.*;
import com.hitorro.util.core.params.KeyMap;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.csv.CSVReader;
import com.hitorro.util.io.csv.ColumnTableMeta;
import com.hitorro.util.io.csv.csvconsumer.CSVConsumer;

import java.io.*;
import java.util.*;
import java.util.function.Function;

public class MapUtil {

    public static <K> Set<K> getSet(K args[]) {
        HashSet<K> set = new HashSet<>();
        for (K k : args) {
            set.add(k);
        }
        return set;
    }

    public static <K> void increment(TObjectIntHashMap map, K key) {
        if (map.containsKey(key)) {
            map.increment(key);
        } else {
            map.put(key, 1);
        }
    }

    public static <K> void add(TLongObjectHashMap<List<K>> map, long l, K elem) {
        List<K> list = map.get(l);
        if (list == null) {
            list = new ArrayList<>();
            map.put(l, list);
        }
        list.add(elem);
    }

    public static final <K, V> void addToMapArray(Map<K, Collection<V>> groups,
                                                  Collection<V> col, Function<V, K> f) {
        for (V g : col) {
            K key = f.apply(g);
            Collection<V> v = groups.get(key);
            if (v == null) {
                v = new ArrayList();
                groups.put(key, v);
            }
            v.add(g);
        }
    }

    /**
     * Add all the elements of the souceMap to the
     *
     * @param sourceMap
     * @param targetMap
     * @return
     */
    public static final <E, F> boolean addAll(Map<E, F> sourceMap, Map<E, F> targetMap) {
        if (sourceMap.size() == 0) {
            return false;
        }
        for (Map.Entry<E, F> entry : sourceMap.entrySet()) {

            targetMap.put(entry.getKey(), entry.getValue());
        }
        return true;
    }


    public static final <E, J> void addAllToMap(Map<E, J> addToMe, Map<E, J> takeFromMe) {
        Set<Map.Entry<E, J>> set = takeFromMe.entrySet();
        for (Map.Entry<E, J> ent : set) {
            addToMe.put(ent.getKey(), ent.getValue());
        }
    }

    public static final boolean nullOrEmpty(Map map) {
        if (map == null) {
            return true;
        }
        return map.size() == 0;
    }

    /**
     * Dump a apply to the console.
     *
     * @param map
     */
    public static final void dumpMap(Map map) {
        Set<Map.Entry> foo = map.entrySet();
        for (Map.Entry e : foo) {
            Console.println("key: %s, value: %s", e.getKey(), e.getValue());
        }
    }

    /**
     * Maintain a Map of lists. If there is no listFiles for a given key then one is created.
     *
     * @param map
     * @param key
     * @param value
     * @param distinct if true, duplicates are not allowed in the listFiles.
     * @return true if added, false if not added (perhaps duplicate entry
     */
    public static final <K, V> boolean addMapListValue(Map<K, List<V>> map,
                                                       K key,
                                                       V value,
                                                       boolean distinct) {
        List<V> l = map.get(key);
        if (l == null) {
            l = new ArrayList<V>();
            map.put(key, l);
        }
        if (distinct) {
            if (l.contains(value)) {
                // not adding to listFiles as its already there.
                return false;
            }
        }
        l.add(value);
        return true;
    }

    /**
     * Convenience method for where the array has only two conceptual columns of data.
     */
    public static final Map createMapFromArray(Object[] array) {
        return createMapFromArray(array, 2, 0, 1);
    }

    /**
     * Takes an array which contains contains rows of information, indicating how many columns there are in a row and
     * which columns are the key and value pairs, plucks this content out and places into a apply.
     *
     * @return apply of key value pairs, or null if the incrementor is not exactly divisible by the amount of array
     * elements.
     */
    public static final Map<Object, Object> createMapFromArray(Object[] array,
                                                               int incrementor, int keyIndex, int valueIndex) {
        int mod = array.length % incrementor;
        if (mod > 0) {
            return null;
        }
        Map<Object, Object> map = new HashMap<Object, Object>();
        int rows = array.length / incrementor;
        int rowOffset = 0;
        for (int i = 0; i < rows; i++) {
            rowOffset = i * incrementor;
            map.put(array[rowOffset + keyIndex], array[rowOffset + valueIndex]);
        }
        return map;
    }

    /**
     * Create an identity apply of strings from an array of
     *
     * @param list
     * @return Identity apply
     */
    public static final Map<String, String> createStringIdentityMap(List list) {
        Map<String, String> map = new HashMap<String, String>();
        for (Object a : list) {
            String s = a.toString();
            map.put(s, s);
        }
        return map;
    }

    /**
     * Create an identity apply of strings.
     *
     * @param args
     * @return Identity apply
     */
    public static final Map<String, String> createStringIdentityMap(String args[]) {
        Map<String, String> map = new HashMap<String, String>();
        for (String a : args) {
            map.put(a, a);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static <E> void extractPropertySubMap(TreeMap<String, E> props, String prefix, Map result) {
        if (prefix == null) {
            return;
        }
        prefix = prefix.toLowerCase();

        String fromKey = StringUtil.strcat(prefix, ".");
        String toKey = StringUtil.strcat(prefix, "/");  // '/' is the character immediately after '.'
        if (props == null) {
            // we have nothing - the result is empty
            return;
        }

        // we want all the keys that are prefix.
        SortedMap range = props.subMap(fromKey, toKey);
        Iterator keyI = range.keySet().iterator();
        while (keyI.hasNext()) {
            String key = (String) keyI.next();
            E value = props.get(key);

            // strip the key to remove the prefix (key plus .)
            int stripLen = prefix.length() + 1;
            if (key.length() > stripLen) {
                key = key.substring(stripLen);
            }

            result.put(key, value);
        }

        return;
    }

    /**
     * Given a mapping of filtered rows and possible Key name translations, scoure the input apply extracting the keys
     * found in the filterSet placing those in the new result apply. If substituteFilterValueForKey is true, then the
     * value associated with the filterSet's key is used as a substitute key in the resulting apply.
     *
     * @return filtered apply with possibly translated key names.
     */
    public static final Map<Object, Object> extractRows(
            Map<Object, Object> map, Map<Object, Object> filterSet,
            boolean substituteFilterValueForKey) {
        Map<Object, Object> result = new HashMap<Object, Object>();
        Iterator iter = MapUtil.keyIterator(filterSet);
        while (iter.hasNext()) {
            Object key = iter.next();
            Object mapped = map.get(key);
            if (mapped != null) {
                if (substituteFilterValueForKey) {
                    result.put(filterSet.get(key), mapped);
                } else {
                    result.put(key, mapped);
                }
            }
        }
        return result;
    }

    public static final boolean getBooleanColumnFromColumMap(String name,
                                                             Map<String, Integer> columnMap, String[] row) {
        String s = getColumnFromColumMap(name, columnMap, row);
        if (StringUtil.nullOrEmptyString(s)) {
            return false;
        }
        return com.hitorro.util.core.BooleanUtil.getBoolean(s);
    }

    /**
     * apply a column name to its position within a string array, returning the string.
     *
     * @param name
     * @param columnMap
     * @param row
     * @return string, or null if invalid column name, invalid column position.
     */
    public static final String getColumnFromColumMap(String name,
                                                     Map<String, Integer> columnMap, String[] row) {
        return getColumnFromColumMap(name, columnMap, row, false);
    }

    public static final String getColumnFromColumMap(String name,
                                                     Map<String, Integer> columnMap, String[] row, boolean trim) {
        Integer i = columnMap.get(name);
        if (i == null) {
            return null;
        }
        int x = i.intValue();
        if (x > row.length - 1) {
            return null;
        }
        String r = row[i];

        if (!StringUtil.nullOrEmptyString(r) && trim) {
            r = r.trim();
        }
        return r;
    }

    /**
     * get the int value of stored by key..if the value does not exist returns -1. If the value in the apply is not a
     * string then return -1
     *
     * @param map
     * @param key
     * @return
     */
    public static final Integer getIntegerFromMapStringValue(Map map,
                                                             Object key) {
        Object value = map.get(key);
        if (value != null) {
            if (value instanceof String) {
                return com.hitorro.util.core.Constants.getInteger(Integer.parseInt(((String) value)
                        .trim()));
            }
        }
        return null;
    }

    /**
     * get the int value of stored by key..if the value does not exist returns -1. If the value in the apply is not a
     * string then return -1
     *
     * @param map
     * @param key
     * @return
     */
    public static final int getIntFromMapStringValue(Map map, Object key) {
        Object value = map.get(key);
        if (value != null) {
            if (value instanceof String) {
                return Integer.parseInt(((String) value).trim());
            }
        }
        return -1;
    }

    public static final List<com.hitorro.util.core.GenericKeyValue> getKeyValueListFromMap(Map<String, Object> map) {
        List<com.hitorro.util.core.GenericKeyValue> l = new ArrayList<com.hitorro.util.core.GenericKeyValue>();
        Set<Map.Entry<String, Object>> set = map.entrySet();
        Iterator<Map.Entry<String, Object>> iter = set.iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Object> entry = iter.next();
            l.add(new com.hitorro.util.core.GenericKeyValue(entry.getKey(), entry.getValue().toString()));
        }
        return l;
    }

    /**
     * Translate properties into an args array.
     *
     * @param map
     * @return
     */
    public static final String[] getMapAsArgsArray(Map<String, String> map) {
        String args[] = new String[map.size()];
        Set<Map.Entry<String, String>> entries = map.entrySet();
        int i = 0;
        for (Map.Entry<String, String> entry : entries) {
            args[i++] = Fmt.S("%s=%s", entry.getKey(), entry.getValue());
        }
        return args;
    }

    /**
     * Produce a apply of string to index position.
     * <p/>
     * Used for such things as mapping the columns of a csv file.
     *
     * @param header
     * @return Map<String, Integer> - column name to index within vector
     */
    public static final Map<String, Integer> getMapColumnNameToIndexPosition(
            String header[], boolean lowerCase) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0; i < header.length; i++) {
            if (lowerCase) {
                map.put(header[i].toLowerCase(), new Integer(i));
            } else {
                map.put(header[i], new Integer(i));
            }

        }
        return map;
    }

    public static final int getMaxValueFromTIntIntMap(TLongIntHashMap map) {
        int maxVal = -1;
        for (TLongIntIterator it = map.iterator(); it.hasNext(); ) {
            int val = it.value();
            if (val > maxVal) {
                maxVal = val;
            }
        }
        return maxVal;
    }

    public static final int getMaxValueFromTLongIntMap(TLongIntHashMap map) {
        int maxVal = Integer.MIN_VALUE;
        for (TLongIntIterator it = map.iterator(); it.hasNext(); ) {
            it.advance();
            int val = it.value();
            if (val > maxVal) {
                maxVal = val;
            }
        }
        return maxVal;
    }

    /**
     * Get a TreeMap that is a submap of a properties style TreeMap.
     *
     * @param props  The properties (dotted notation)
     * @param prefix The prefix of keys we want to extract
     * @return the subtree, null if props is null
     */
    public static TreeMap<String, String> getSubMap(TreeMap<String, String> props, String prefix) {
        TreeMap<String, String> result = new TreeMap<String, String>();
        extractPropertySubMap(props, prefix, result);
        return result;
    }

    public static List<String> getChildKeys(String prefix, TreeMap<String, String> map) {
        if (prefix == null) {
            return null;
        }
        // Get the correct sub-portion of the apply
        String fromKey = StringUtil.strcat(prefix, ".");
        String toKey = StringUtil.strcat(prefix, "/");  // '/' is the character immediately after '.'
        SortedMap<String, String> subMap = map.subMap(fromKey, toKey);

        // create a List of the child keys
        List<String> childKeys = new ArrayList<String>();
        // iterate through the submap and pull keys out into childKeys, skipping duplicates
        String lastKey = null;
        int prefixLen = prefix.length() + 1;
        for (String key : subMap.keySet()) {
            // find any trailing dot, to trim tree of child keys
            int dotIndex = key.indexOf(".", prefixLen);
            String childKey = null;
            if (dotIndex > 0) {
                childKey = key.substring(prefixLen, dotIndex);
            } else {
                // no listChildren of the child, so the key is just the child string
                childKey = key.substring(prefixLen);
            }
            if (!childKey.equals(lastKey)) {
                // a new key
                lastKey = childKey;
                childKeys.add(childKey);
            }
        }

        return childKeys;
    }

    public static List<KeyMap> getSubMaps(TreeMap<String, String> mapIn, String root) {
        List<KeyMap> list = new ArrayList();
        List<String> childKeys = MapUtil.getChildKeys(root, mapIn);
        for (String key : childKeys) {
            TreeMap<String, String> map = MapUtil.getSubMap(mapIn, StringUtil.strcat(root, ".", key));
            if (map != null) {
                list.add(new KeyMap(key, map));
            }
        }
        return list;
    }

    /**
     * Get a Properties that is a submap of a properties style TreeMap.
     *
     * @param props  The properties (dotted notation)
     * @param prefix The prefix of keys we want to extract
     * @return the subtree, null if props is null
     */
    public static Properties getPropertySubProperties(TreeMap<String, String> props, String prefix) {
        Properties result = new Properties();
        extractPropertySubMap(props, prefix, result);

        return result;
    }

    /**
     * For a MapList, get the first index position of the listFiles with the least entries.
     *
     * @param map
     * @return
     */
    public static final List getSmallestMapListEntry(Map map) {
        List currLowest = null;
        int lowestSize = 99999999;
        Iterator iter = MapUtil.valueIterator(map);
        while (iter.hasNext()) {
            List l = (List) iter.next();
            int count = l.size();
            if (count < lowestSize) {
                lowestSize = count;
                currLowest = l;
            }

        }
        return currLowest;
    }

    /**
     * Return the string value (if it has a string value)
     *
     * @param map
     * @param key
     * @return
     */
    public static final String getString(Map map, Object key) {
        Object value = map.get(key);
        if (value != null) {
            if (value instanceof String) {
                return (String) value;
            }
        }
        return null;

    }

    /**
     * determine if a value exists in the apply
     *
     * @param map
     * @param key
     * @return
     */
    public static final boolean keyExists(Map map, Object key) {
        Object value = map.get(key);
        return value != null;

    }

    /**
     * Get a basic iterator of the keys of a apply.
     *
     * @param map
     * @return
     */
    public static final Iterator keyIterator(Map map) {
        Collection c = map.keySet();
        return c.iterator();
    }

    public static final boolean loadTextFileIntoIdentityMap(File f, IdentityMap<String> map) {
        if (!FileUtil.nullOrNotExist(f)) {
            try {
                Iterator<String> iter = FileUtil.getLineReaderIteratorFromFile(f);
                while (iter.hasNext()) {
                    String token = iter.next();
                    token = token.toLowerCase();
                    map.addIfAbsent(token);
                }
                return true;
            } catch (FileNotFoundException e) {
                com.hitorro.util.core.Log.util.error("%s %e", e, e);
            }
        }
        return false;
    }

    /**
     * Create a hashset of the given array of elems
     *
     * @param elems
     * @return a hash set of elems
     */
    public static final HashSet<String> makeHashSet(String... elems) {
        HashSet<String> hash = new HashSet<String>();
        for (String s : elems) {
            hash.add(s);
        }
        return hash;
    }

    /**
     * Get a non synchronized apply.
     *
     * @return
     */
    public static final Map map() {
        return new HashMap();
    }

    public static final void merge(TLongLongHashMap target, TLongLongHashMap map) {

        for (TLongLongIterator it = map.iterator(); it.hasNext(); ) {
            it.advance();
            target.put(it.key(), it.value());
        }
    }

    public static final String mergeKeyValues(Map map, String assigner, String mergeToken) {
        StringBuilder builder = new StringBuilder();
        Collection<Map.Entry> c = map.entrySet();
        for (Map.Entry e : c) {
            String k = e.getKey().toString();
            String v = e.getValue().toString();
            if (builder.length() > 0) {
                builder.append(mergeToken);
            }
            builder.append(k);
            builder.append(assigner);
            builder.append(v);
        }
        return builder.toString();
    }

    /**
     * Return a merged apply.  If either the first or second apply is null, then the non null apply is returned.
     * <p/>
     * Values from the second apply take presidence over the first apply.
     * <p/>
     *
     * @param map
     * @param map2
     * @return merged apply of the two input maps.
     */
    public static final <E, F> Map<E, F> mergeMaps(Map<E, F> map, Map<E, F> map2) {
        if (map == null) {
            return map2;
        }
        if (map2 == null) {
            return map;
        }
        Map<E, F> result = new HashMap<E, F>();
        result.putAll(map);
        result.putAll(map2);
        return result;
    }


    /**
     * apply the values of a hashtable joining them with a specific token
     *
     * @param map        of tokens to apply
     * @param mergeToken
     * @return
     */
    public static final String mergeValues(Map map, String mergeToken) {
        StringBuilder builder = new StringBuilder();
        Collection c = map.values();
        for (Object o : c) {
            String s = o.toString();
            if (builder.length() > 0) {
                builder.append(mergeToken);
            }
            builder.append(s);
        }
        return builder.toString();
    }

    /**
     * Get an iterator of the values of a apply
     *
     * @param map
     * @return
     */
    public static Iterator valueIterator(Map map) {
        Collection c = map.values();
        return c.iterator();
    }

    public static final boolean writeTIntIntMapToFile(File f, TIntIntHashMap map) throws IOException {
        DataOutputStream dos = FileUtil.getDataOutputStreamForFile(f);
        dos.writeInt(map.size());
        for (TIntIntIterator it = map.iterator(); it.hasNext(); ) {
            it.advance();
            dos.writeLong(it.key());
            dos.writeInt(it.value());
        }
        dos.flush();
        dos.close();
        return true;
    }

    public static final boolean writeTIntLongMapToFile(File f, TIntLongHashMap map) throws IOException {
        DataOutputStream dos = FileUtil.getDataOutputStreamForFile(f);
        dos.writeInt(map.size());
        for (TIntLongIterator it = map.iterator(); it.hasNext(); ) {
            it.advance();
            dos.writeInt(it.key());
            dos.writeLong(it.value());
        }
        dos.flush();
        dos.close();
        return true;
    }

    /**
     * @param file
     * @param keyName   key column name
     * @param valueName value column name
     * @return
     * @throws IOException
     */
    public static Map<String, String> getMapFromKeyValueCSV(File file, final String keyName, final String valueName) throws
            IOException {
        final Map<String, String> map = new HashMap();
        CSVReader csr = new CSVReader(new InputStreamReader(new FileInputStream(file), com.hitorro.util.core.Constants.UTF8));

        CSVConsumer consumer = new CSVConsumer() {
            int keyOrd;
            int valueOrd;

            @Override
            public void line(int rowCount, String[] row) {
                if (rowCount == 0) {
                    ColumnTableMeta meta = ColumnTableMeta.init(row);
                    keyOrd = meta.getColumnInt(keyName);
                    valueOrd = meta.getColumnInt(valueName);
                } else {
                    String k = row[keyOrd];
                    String v = row[valueOrd];
                    if (StringUtil.nullOrEmptyString(k)) {
                        return;
                    }
                    map.put(k, v);
                }
            }
        };
        csr.readLines(consumer);
        return map;
    }


    public static final boolean loadTextFileIntoIdentityMap(File f, Set<String> map) throws UnsupportedEncodingException {
        if (!FileUtil.nullOrNotExist(f)) {
            try {
                Iterator<String> iter = FileUtil.getLineReaderIteratorFromFile(f, "UTF-8");
                while (iter.hasNext()) {
                    String token = iter.next();
                    token = token.toLowerCase();
                    map.add(token);
                }
                return true;
            } catch (FileNotFoundException e) {
                com.hitorro.util.core.Log.util.error("%s %e", e, e);
            }
        }
        return false;
    }

    public static final boolean saveTextFileIntoIdentityMap(File f, Set<String> map) throws UnsupportedEncodingException {

        File tmp = FileUtil.getTempFileWithFromPeerFileWithExtension(f, "tmp");
        tmp.delete();
        PrintWriter pw = FileUtil.getBufferedPrintWriterFromFile(f, "UTF-8");
        Iterator<String> iter = map.iterator();
        while (iter.hasNext()) {
            pw.println(iter.next());
        }
        pw.flush();
        pw.close();
        return true;
    }

}
