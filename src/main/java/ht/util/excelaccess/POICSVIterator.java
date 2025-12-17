package ht.util.excelaccess;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.iterator.AbstractIterator;
import ht.util.io.FileUtil;
import ht.util.io.csv.CSVIterator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Iterate through a sheet of an excel spreadsheet in a naive way.  We assume the first row are the headings
 */
public class POICSVIterator extends AbstractIterator<String[]> implements CSVIterator {
    private POISpreadSheetBook m_book;
    private POISpreadSheetSheet sheet = null;
    private String header[];
    private int rows;
    private int i;

    public POICSVIterator(File file, String sheetName, boolean hasHeader) throws IOException {
        setupBook(FileUtil.getBufferedFileInputStream(file), sheetName, hasHeader);

    }

    public POICSVIterator(BaseFile file, String sheetName, boolean hasHeader) throws IOException {
        setupBook(file.getDataInputStream(), sheetName, hasHeader);
    }

    public POICSVIterator(InputStream is, String sheetName, boolean hasHeader) throws IOException {
        setupBook(is, sheetName, hasHeader);
    }

    public POICSVIterator() {
    }

    public String[] getColumnNames() {
        return header;
    }

    public boolean setupBook(InputStream stream, String sheetName, boolean hasHeader)
            throws IOException {
        POISpreadSheet spreadSheet = new POISpreadSheet();
        m_book = spreadSheet.getBook(stream);
        if (!setupBook(sheetName)) {
            return false;
        }
        if (hasNext()) {
            header = next();
        }
        return false;
    }


    /**
     * @param sheetName
     * @return true if page exists and was read
     */
    private boolean setupBook(String sheetName) {
        int numSheets = m_book.getSheetCount();

        for (int i = 0; i < numSheets; i++) {
            POISpreadSheetSheet s = m_book.getSpreadSheet(i);
            String name = s.getName();
            if (sheetName == null || name.equalsIgnoreCase(sheetName)) {
                sheet = s;
                rows = sheet.getLastRow();
                return true;
            }
        }
        return false;
    }

    private String[] rowToLine() {
        int columns = sheet.getLastColumnForRow(i);
        String s[] = new String[columns + 1];
        for (int column = 0; column <= columns; column++) {
            s[column] = (sheet.getText(i, column));
        }
        i++;
        return s;
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public boolean hasNext() {
        return i <= rows;
    }

    @Override
    public String[] next() {
        return rowToLine();
    }

    @Override
    public void remove() {
    }
}
