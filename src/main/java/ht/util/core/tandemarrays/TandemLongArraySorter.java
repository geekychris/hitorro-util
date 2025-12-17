package ht.util.core.tandemarrays;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 * <p/>
 * sort an array of integers and at the same time sort a peer array encapsulated in the TandemArraySorterPeer.
 */
public class TandemLongArraySorter {

    private int beg;
    private int end;
    private long[] list;
    private TandemArraySorterPeer m_peer;

    public TandemLongArraySorter() {
        /*Default constructor*/

        list = null;
        beg = 0;
        end = 0;
    }

    private void insertion_sort() {
        /*Sorts the listFiles "sorted" by use of the insertion sort algorithm.
     To conserve memory, the sorted listFiles is placed at the front of the
       unsorted listFiles.  As the unsorted listFiles shrinks, the sorted listFiles grows.
     The last element of the sorted listFiles comes just before the first element
     in the unsorted listFiles.*/

        Integer tmp = null;     //Temporary variable used for swapping

        for (int i = beg + 1; i < end; ++i) {
            //Move backwards from the end of the sorted listFiles to the front
            for (int s = i; s > beg; --s) {
                //If the current element is less than the value before it
                if (list[s] < list[s - 1]) {
                    swap(s);
                } else {
                    break;
                }
            }
        }
    }

    private void swap(int s) {
        long tmp;//Swap those elements
        tmp = list[s];
        list[s] = list[s - 1];
        list[s - 1] = tmp;
        m_peer.swap(s);
    }

    public void sort(long[] unsorted, TandemArraySorterPeer peer) {
        /*Initializes listFiles to the copy of the passed unsorted array.
   It then invokes the insertion sort algorithm on that listFiles.
  The resulting sorted listFiles is then returned.*/

        m_peer = peer;
        list = unsorted;
        beg = 0;
        end = list.length;

        insertion_sort();

    }

    public void sortSublist(long[] unsorted, int a, int b) {
        /*Sorts a sublist from a to b in a bigger array, "listFiles"*/

        beg = a;
        end = b + 1;
        list = unsorted;

        insertion_sort();
    }
}
