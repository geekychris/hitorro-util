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
package com.hitorro.util.core.tandemarrays;

/**

 * sort an array of integers and at the same time sort a peer array encapsulated in the TandemArraySorterPeer.
 */
public class TandemIntArraySorter {
    private int beg;
    private int end;
    private int[] list;
    private TandemArraySorterPeer m_peer;

    public TandemIntArraySorter() {
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

    private final void swap(int s) {
        int tmp;//Swap those elements
        tmp = list[s];
        list[s] = list[s - 1];
        list[s - 1] = tmp;
        m_peer.swap(s);
    }

    public void sort(int[] unsorted, TandemArraySorterPeer peer) {
        /*Initializes listFiles to the copy of the passed unsorted array.
   It then invokes the insertion sort algorithm on that listFiles.
  The resulting sorted listFiles is then returned.*/

        m_peer = peer;
        list = unsorted;
        beg = 0;
        end = list.length;

        insertion_sort();

    }

    public void sortSublist(int[] unsorted, int a, int b) {
        /*Sorts a sublist from a to b in a bigger array, "listFiles"*/

        beg = a;
        end = b + 1;
        list = unsorted;

        insertion_sort();
    }
}
