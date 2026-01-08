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
package com.hitorro.util.excelaccess;

import com.hitorro.util.core.string.Fmt;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_NULL_AND_BLANK;


/**
 */
public class POISpreadSheetSheet {
    private HSSFSheet sheet;
    private HSSFWorkbook book;
    private int currentSheetNum;

    public POISpreadSheetSheet(HSSFSheet sheetIn, HSSFWorkbook bookIn, int sheetNumber) {
        sheet = sheetIn;
        book = bookIn;
        currentSheetNum = sheetNumber;
    }

    public CellType getCellType(int rowNum, int columnNum) {
        HSSFCell cell = retrieveCell(rowNum, columnNum);
        return cell.getCellType();
    }

    public void setFormula(int rowNum, int columnNum, String formula) {
        HSSFCell cell = findOrCreateCell(rowNum, columnNum);
        cell.setCellType(CellType.FORMULA);
        cell.setCellValue(formula);
    }

    public String getText(int rowNum, int columnNum) {
        HSSFCell cell = retrieveCell(rowNum, columnNum);
        CellType type = cell.getCellType();
        String value = null;
        try {
            switch (type) {
                case BOOLEAN:
                    boolean boolVal = cell.getBooleanCellValue();
                    value = String.valueOf(boolVal).toUpperCase();
                    break;

                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        Date userDate = cell.getDateCellValue();
                        Calendar calendar = new GregorianCalendar();

                        calendar.setTime(userDate);

                        if ((calendar.get(Calendar.HOUR) == 0)
                                && (calendar.get(Calendar.MINUTE) == 0)
                                && (calendar.get(Calendar.SECOND) == 0)) {
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                            value = formatter.format(userDate);
                        } else {
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            value = formatter.format(userDate);
                        }
                    } else {
                        double doubleVal = cell.getNumericCellValue();
                        NumberFormat f = NumberFormat.getInstance();
                        f.setGroupingUsed(false);
                        value = f.format(doubleVal);
                    }
                    break;
                case STRING:
                    value = cell.getStringCellValue();
                    break;
                case BLANK:
                    value = cell.getStringCellValue();
                    break;
                default:
                    throw new DetailedSpreadSheetException(
                            Fmt.S("POISpreadSheetSheet.getText : Unexpected cell type -> %s", type),
                            currentSheetNum,
                            rowNum,
                            columnNum);
            }
        } catch (NumberFormatException e) {
            throw new DetailedSpreadSheetException(e, currentSheetNum, rowNum, columnNum);
        }
        return value;

    }


    /**
     * Get the Last row for this sheet
     *
     * @return last row number
     */

    public int getLastRow() {
        return sheet.getLastRowNum();
    }


    /**
     * Get the last column for this row.
     *
     * @param rowNum The specified row number  [0 based row numbering]
     * @return last column index for this row
     */

    public int getLastColumnForRow(int rowNum) {
        HSSFRow row = retrieveRow(rowNum);
        return (row.getLastCellNum());
    }


    public String getName() {
        return book.getSheetName(currentSheetNum);
    }

    public void setName(String name) {
        book.setSheetName(currentSheetNum, name);
    }

    public void setText(int rowNum, int colNum, String text) {
        HSSFCell cell = findOrCreateCell(rowNum, colNum);
        cell.setCellValue(text);
    }

    public void setFontColor(int rowNum, int colNum, int color) {
        HSSFCell cell = findOrCreateCell(rowNum, colNum);
        HSSFCellStyle style = book.createCellStyle();
        HSSFFont font = book.createFont();
        font.setColor((short) color);
        style.setFont(font);
        cell.setCellStyle(style);

    }

    public void setBold(int rowNum, int colNum, boolean bold) {
        HSSFCell cell = findOrCreateCell(rowNum, colNum);
        HSSFCellStyle style = book.createCellStyle();
        HSSFFont font = book.createFont();
        font.setBold(bold);
        style.setFont(font);
        cell.setCellStyle(style);
    }

    public void setFontSize(int rowNum, int colNum, int fontSize) {
        HSSFCell cell = findOrCreateCell(rowNum, colNum);
        HSSFCellStyle style = book.createCellStyle();
        HSSFFont font = book.createFont();
        font.setFontHeightInPoints((short) fontSize);
        style.setFont(font);
        cell.setCellStyle(style);
    }


    public void setBackgroundColor(int rowNum, int colNum, int color) {
        HSSFCell cell = findOrCreateCell(rowNum, colNum);
        HSSFCellStyle style = book.createCellStyle();
        style.setFillBackgroundColor((short) color);
        cell.setCellStyle(style);
    }

    public void setUnderline(int rowNum, int colNum, boolean underline) {
        HSSFCell cell = findOrCreateCell(rowNum, colNum);
        HSSFCellStyle style = book.createCellStyle();
        HSSFFont font = book.createFont();
        if (underline == true) {
            font.setUnderline(HSSFFont.U_SINGLE);
        } else {
            font.setUnderline(HSSFFont.U_NONE);
        }
        style.setFont(font);
        cell.setCellStyle(style);
    }

    // ----------------------------- Helper methods --------------------------


    /**
     * Helper method that fetches a HSSFRow from the current sheet given the appropriate index. Use this
     * <p/>
     * method if you expect the row to be there (eg. reading a file)
     */

    private HSSFRow retrieveRow(int rowNum) {
        HSSFRow row = sheet.getRow(rowNum);
        if (row == null) {
            if (rowNum > getLastRow()) {
                throw new DetailedSpreadSheetException(
                        Fmt.S("POISpreadSheetSheet : Row does not exist: %s", rowNum),
                        currentSheetNum,
                        rowNum,
                        -1);
            }
            row = sheet.createRow((short) rowNum);
        }
        return row;
    }

    private HSSFRow findOrCreateRow(int rowNum) {
        HSSFRow row = sheet.getRow(rowNum);
        if (row == null) {
            row = sheet.createRow((short) rowNum);
        }
        return row;
    }

    private HSSFCell retrieveCell(int rowNum, int columnNum) {
        HSSFRow row = retrieveRow(rowNum);
        HSSFCell cell = row.getCell((short) columnNum, RETURN_NULL_AND_BLANK);

        if (cell == null) {
            if (columnNum > getLastColumnForRow(rowNum)) {
                throw new DetailedSpreadSheetException(
                        Fmt.S("POISpreadSheetSheet.getText : Column does not exist: %s", columnNum),
                        currentSheetNum,
                        rowNum,
                        columnNum);
            }
            cell = row.createCell(columnNum);
        }
        return cell;
    }


    private HSSFCell findOrCreateCell(int rowNum, int columnNum) {
        HSSFRow row = findOrCreateRow(rowNum);
        HSSFCell cell = row.getCell((short) columnNum, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            cell = row.createCell(columnNum);
        }
        return cell;
    }
}

