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
package com.hitorro.util.core.iterator.database;

import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.mappers.BaseMapper;

import java.sql.*;


/**
 * Iterate a jdbc collection, mapping the row elements via a JDBCFieldFetcherValueAdapter into the adapters target
 * object.
 *
 * @param <E> type of the resultant object returned from the iterator.
 * @author chris
 * @see JDBCFieldFetcherValueAdapter
 */
public class JDBCIterator<E> extends AbstractIterator<E> {
    private BaseMapper<ResultSet, E> m_adapter;
    private String m_select;
    private ResultSet m_rs;
    private int cnt = 0;
    private boolean m_hasNext = false;
    private E m_returnMe;
    private Connection m_connection = null;
    private int rowsRetrieved = 0;

    private JDBCIterator() {

    }

    /**
     * constructor.  Provide a basic sql statement and the adapter to consume the result rows.
     * <p/>
     * Currently does not allow you to set the parameters of a prepared statement.
     *
     * @param selectStatement
     * @param adapter
     */
    public JDBCIterator(String selectStatement,
                        BaseMapper<ResultSet, E> adapter,
                        Connection connection,
                        Object args[]) {
        m_select = selectStatement;
        m_adapter = adapter;

        PreparedStatement pstmt = null;
        try {
            m_connection = connection;
            pstmt = m_connection.prepareStatement(m_select, ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            pstmt.setFetchSize(1000);
            if (args != null && args.length > 0) {
                for (int i = 1; i <= args.length; i++) {
                    Object o = args[i - 1];
                    if (o instanceof Date) {
                        pstmt.setDate(i, (Date) o);
                    } else if (o instanceof java.util.Date) {
                        Date d = new Date(((java.util.Date) o).getTime());
                        pstmt.setDate(i, d);
                    } else {
                        pstmt.setString(i, o.toString());
                    }

                }
            }
            m_rs = pstmt.executeQuery();
        } catch (SQLException sqle) {
            Log.jdbc.error("Unable to execute JDBCIterator %s %e", sqle, sqle);
        }
        m_hasNext = hasNextAux();
    }

    public int getRowsRetrieved() {
        return rowsRetrieved;
    }

    public boolean hasNext() {
        return m_hasNext;
    }

    private boolean hasNextAux() {
        try {
            if (m_rs.next()) {
                rowsRetrieved++;
                m_returnMe = m_adapter.apply(m_rs);
            } else {
                m_adapter.close();
                return false;
            }
        } catch (SQLException e) {
            Log.util.error("Unable to get next %s %e", e, e);
            return false;
        }
        return true;
    }

    public E next() {
        E returnMe = m_returnMe;

        m_hasNext = hasNextAux();
        if (!m_hasNext) {
            close();
        }
        return returnMe;
    }

    public void remove() {

    }

    public void close() {
        try {
            if (m_rs == null) {
                return;
            }

            m_rs.close();
        } catch (SQLException e) {
            Log.util.error("Unable to close jdbc connection %s %e", e, e);
        }
    }


    /**
     * Not a big fan of finalizers, but incase we exit without iterating all the items and we did not call "close" on
     * this iterator then hopefully we free the connection through this mechanism.
     * <p/>
     * There are a bazillion reasons to not use a finalizer (lock issues, may never execute on some vm's, etc).
     */
    public void finalizer() {
        close();
    }

}
