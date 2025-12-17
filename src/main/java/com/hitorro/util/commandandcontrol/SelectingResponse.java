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
package com.hitorro.util.commandandcontrol;

import com.hitorro.util.core.string.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide a mechanism to treat a response like a table.
 * The response can be:
 * - pruned by column name (select a,b,c... vs select *)
 * - sorted by column criteria (order by a,c .... order by a,c desc)
 * - filtered by a column criteria (where a> 10)
 */
public class SelectingResponse extends Response {
    private boolean m_endCalled = false;

    private Response m_chain;

    private List<ResponseRow> m_rows = new ArrayList<ResponseRow>();

    private List<RenderingContainer[]> renderingContainers = new ArrayList();

    private List<LevelMsg> infoMessages = new ArrayList<LevelMsg>();

    private List<String> headerRows = new ArrayList<String>();

    private SelectingResponse() {

    }

    public SelectingResponse(Response r, int packingSize) {
        m_chain = r;
    }

    public void setCommandSession(CommandSession cs) {
        if (m_chain != null) {
            m_chain.setCommandSession(cs);
        }
    }

    @Override
    public void addStatusUpdateMessage(final String info, final int percentComplete) {
        // do nothing

        m_chain.addStatusUpdateMessage(info, percentComplete);
    }

    public void addInfo(InfoLevel level, String info) {
        infoMessages.add(new LevelMsg(level, info));
    }

    public void setResponseShape(ResponseShape s) {
        // here we put the header.
        super.setResponseShape(s);
        m_chain.setResponseShape(shape);

    }

    @Override
    public void addRow(Object... elements) {
        addRowArray(elements);
    }

    public void addRowArray(Object elements[]) {
        ResponseRow row;
        row = new ResponseRow(elements);
        m_rows.add(row);
        if (this.containers == null) {
            renderingContainers.add(new RenderingContainer[0]);
        } else {
            renderingContainers.add(this.containers);
        }
        this.containers = null;
    }


    @Override
    public void end() {
        if (m_endCalled) {
            return;
        }
        m_endCalled = true;
        // header rows
        for (String header : headerRows) {
            m_chain.addBannerRow(header);
        }
        // column names for table data.
        if (shape != null && shape.m_header != null) {

            //shape.m_header.addRow(m_chain, packingSizeBuff);
            int size = m_rows.size();
            for (int i = 0; i < size; i++) {

                ResponseRow row = m_rows.get(i);
                row.setRenderingContainer(this.renderingContainers.get(i));
                //row.addRow(m_chain, packingSizeBuff);

            }
        }

        for (LevelMsg info : infoMessages) {
            m_chain.addInfo(info.getKey(), info.getValue());
        }
    }

    @Override
    public void addBannerRow(String row) {
        headerRows.add(row);
    }
}

class SelectResponseRow {
    private int m_truncateToMaxLength = -1;

    private String[] m_values;
    private RenderingContainer[] renderingContainer;

    public SelectResponseRow(Object args[]) {
        set(args);
    }

    public String[] getHeader() {
        return m_values;
    }

    public void setRenderingContainer(RenderingContainer[] container) {
        renderingContainer = container;
    }

    public void setMaxColumnSize(int size) {
        m_truncateToMaxLength = size;
    }

    public void set(Object args[]) {
        m_values = StringUtil.objectArrayToString(args, "");
    }

    public int getLength(int column) {
        if (column < m_values.length) {
            return m_values[column].length();
        }
        return -1;
    }

    public void addRow(Response response, int pack[]) {
        response.setRenderingRow(this.renderingContainer);
        // Where to choose columns.
        response.addRow((Object[]) StringUtil.pack(m_values, pack, ' '));
    }

    public int[] getSizes() {
        int sizes[] = new int[m_values.length];
        return growSizes(sizes);
    }

    /**
     * Grow the size of each column to fit that of the column element held in this row.
     *
     * @param sizes
     * @return
     */
    public int[] growSizes(int sizes[]) {
        int min = Math.min(m_values.length, sizes.length);
        for (int i = 0; i < min; i++) {
            int s = m_values[i].length();
            if (sizes[i] < s) {
                if (m_truncateToMaxLength > -1) {
                    if (s > m_truncateToMaxLength) {
                        sizes[i] = m_truncateToMaxLength;
                    } else {
                        sizes[i] = s;
                    }
                } else {
                    sizes[i] = s;
                }
            }
        }
        return sizes;

    }
}
