/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package com.scireum.dd;

import com.scireum.App;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.NLS;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Used to write an Excel file from JavaScript
 * <p>
 * Used by donkey.js
 */
public class OutputExcel {

    private final HSSFWorkbook workbook;
    private final HSSFSheet sheet;
    private int rows = 0;
    private int maxCols = 0;
    private HSSFCellStyle dateStyle;
    private HSSFCellStyle numeric;
    private HSSFCellStyle borderStyle;
    private HSSFCellStyle normalStyle;

    /**
     * Generates a new Export
     */
    public OutputExcel() {
        workbook = new HSSFWorkbook();
        sheet = workbook.createSheet();
        // Setup styles
        dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd.mm.yyyy"));
        numeric = workbook.createCellStyle();
        numeric.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        borderStyle = workbook.createCellStyle();
        borderStyle.setBorderBottom(HSSFCellStyle.BORDER_THICK);
        normalStyle = workbook.createCellStyle();
        // Setup layout
        sheet.createFreezePane(0, 1, 0, 1);
        HSSFPrintSetup ps = sheet.getPrintSetup();
        ps.setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);
        ps.setLandscape(false);
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);
        sheet.setAutobreaks(true);
        sheet.setRepeatingRows(new CellRangeAddress(0, 0, -1, -1));
    }

    private void addCell(HSSFRow row, Object obj, int columnIndex, HSSFCellStyle style) {
        if (obj == null) {
            return;
        }
        HSSFCell cell = row.createCell(columnIndex);
        cell.setCellStyle(style);
        if (obj instanceof String) {
            cell.setCellValue(new HSSFRichTextString((String) obj));
            return;
        }
        if (obj instanceof LocalDateTime) {
            cell.setCellValue(Date.from(((LocalDateTime) obj).atZone(ZoneId.systemDefault()).toInstant()));
            return;
        }
        if (obj instanceof LocalDate) {
            cell.setCellValue(Date.from(((LocalDate) obj).atStartOfDay(ZoneId.systemDefault()).toInstant()));
            return;
        }
        if (obj instanceof Boolean) {
            cell.setCellValue(new HSSFRichTextString(NLS.toUserString(obj)));
            return;
        }
        if (obj instanceof Double) {
            cell.setCellValue((Double) obj);
            return;
        }
        if (obj instanceof Float) {
            cell.setCellValue((Float) obj);
            return;
        }
        if (obj instanceof Integer) {
            cell.setCellValue((Integer) obj);
            return;
        }
        if (obj instanceof Long) {
            cell.setCellValue((Long) obj);
            return;
        }
        if (obj instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) obj).doubleValue());
            return;
        }
        cell.setCellValue(new HSSFRichTextString(obj.toString()));
    }

    /*
     * Bridge method used by donkey.js
     */
    public OutputExcel addRow(Object... row) {
        addRow(Arrays.asList(row));
        return this;
    }

    /*
     * Bridge method used by donkey.js
     */
    public OutputExcel addRow(List<?> row) {
        if (row != null) {
            maxCols = Math.max(maxCols, row.size());
            int idx = 0;
            HSSFRow r = sheet.createRow(rows++);
            for (Object entry : row) {
                addCell(r, entry, idx++, getCellStyleForObject(entry));
            }
        }
        return this;
    }

    private void writeToStream(OutputStream out) {
        try {
            try {
                // Make it pretty...
                for (short col = 0; col < maxCols; col++) {
                    sheet.autoSizeColumn(col);
                }
                // Add autofilter...
                sheet.setAutoFilter(new CellRangeAddress(0, rows, 0, maxCols - 1));
                workbook.write(out);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw Exceptions.handle(e);
        }
    }

    private HSSFCellStyle getCellStyleForObject(Object data) {
        HSSFCellStyle style = normalStyle;
        if (data instanceof LocalDate || data instanceof LocalDateTime) {
            style = dateStyle;
        } else if (data instanceof Integer || data instanceof Double || data instanceof Long) {
            style = numeric;
        }
        return style;
    }

    /*
     * Bridge method used by donkey.js
     */
    public void save(String filename) {
        try {
            App.LOG.INFO("Saving %s lines to: %s", rows, filename);
            try (FileOutputStream out = new FileOutputStream(filename)) {
                writeToStream(out);
            }
        } catch (IOException e) {
            Exceptions.handle(e);
        }
    }
}
