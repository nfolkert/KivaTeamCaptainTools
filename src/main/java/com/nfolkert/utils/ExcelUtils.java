package com.nfolkert.utils;

/**
 */
public class ExcelUtils
{
    /*
    public static WritableWorkbook createExcelFile(File file)
            throws BaseException
    {
        try
        {
            File parentDir = file.getParentFile();
            parentDir.mkdirs();
            return Workbook.createWorkbook(file, JXLUtils.createDefaultSettings(null));
        }
        catch (Exception ex)
        {
            throw BaseException.castOrCreate(ex);
        }
    }

    public static String workbookToString(File file)
            throws BaseException
    {
        try
        {
            if (file.exists() && file.length() == 0)
            {
                return "";
            }
            final Workbook book = Workbook.getWorkbook(file, JXLUtils.createDefaultSettings(null));
            final StringBuilder result = new StringBuilder();
            for (int i = 0; i < book.getSheetNames().length; i++)
            {
                final Sheet sheet = book.getSheet(i);
                final String sheetName = sheet.getName();
                result.append(sheetName);
                result.append("\n");

                for (int k = 0; k < sheet.getNumberOfImages(); k++)
                {
                    final Image drawing = sheet.getDrawing(k);
                    result.append("Drawing at (" + drawing.getColumn() + ", " + drawing.getRow() + ")");
                    result.append("\n");
                }

                for (int j = 0; j < sheet.getRows(); j++)
                {
                    for (int k = 0; k < sheet.getColumns(); k++)
                    {
                        final Cell cell = sheet.getCell(k, j);
                        if (k != 0)
                            result.append('\t');

                        final String toPrint;
                        if (cell instanceof NumberFormulaCell)
                            toPrint = "=" + ((NumberFormulaCell) cell).getFormula();
                        else toPrint = cell.getContents();
                        result.append(toPrint);
                    }
                    result.append("\n");
                }
                result.append("\n");
            }
            book.close();
            return result.toString();
        }
        catch (Exception e)
        {
            throw new BaseException("Exception occurred reading workbook", e);
        }

    }

    public static String[][] readTableFromExcelFileOrCSV(final File file,
                                                         final String optionalTabName,
                                                         final boolean filterEmptyAndComments,
                                                         final boolean keepHeader,
                                                         final boolean trimWhitespace,
                                                         final boolean justHeader)
        throws BaseException
    {
        if (!keepHeader && justHeader) throw new IllegalArgumentException();
        if (file == null || !file.exists()) return null;
        try
        {
            String fileName = file.getName();
            if (fileName.endsWith(".csv"))
            {
                return readTableFromCSV(file, filterEmptyAndComments, keepHeader, trimWhitespace, justHeader);
            }
            else if (fileName.endsWith(".xls"))
            {
                return readTableFromExcel(file, optionalTabName, filterEmptyAndComments, keepHeader, trimWhitespace, justHeader);
            }
            else throw new IllegalArgumentException();
        }
        catch(Exception ex)
        {
                throw BaseException.castOrCreate(ex);
        }
    }

    public static String[] readExcelTabNames(final File file)
            throws BiffException, IOException
    {
        final Workbook wb = Workbook.getWorkbook(file, JXLUtils.createDefaultSettings(null));
        return wb.getSheetNames();
    }

    private static String[][] readTableFromExcel(final File file,
                                                 final String optionalTabName,
                                                 final boolean filterEmptyAndComments,
                                                 final boolean keepHeader,
                                                 final boolean trimWhitespace,
                                                 final boolean justHeader)
            throws IOException, BiffException
    {
        final Workbook wb = Workbook.getWorkbook(file, JXLUtils.createDefaultSettings(null));

        final Sheet sheet;
        if (optionalTabName != null)
            sheet = wb.getSheet(optionalTabName);
        else
            sheet = wb.getSheet(0);
        if (sheet == null) return null;

        List<String[]> ret = new ArrayList<String[]>();

        for (int i = justHeader ? 0 : keepHeader ? 0 : 1; i < (justHeader ? 1 : sheet.getRows()); i++)
        {
            Cell[] rowCells = sheet.getRow(i);
            if (!filterEmptyAndComments || !ConfigManipulator.isRowCommentedOrEmpty(rowCells))
            {
                String[] row = new String[rowCells.length];
                for (int j = 0; j < row.length; j++)
                    row[j] = getCellStringValue(rowCells[j], trimWhitespace);
                ret.add(row);
            }
        }
        return ret.toArray(new String[ret.size()][]);
    }

    public static Serializable[][] readTypedTableFromExcel(final File file,
                                                           final String optionalTabName)
        throws IOException, BiffException, BaseException
    {
        final Workbook wb = Workbook.getWorkbook(file, JXLUtils.createDefaultSettings(null));

        final Sheet sheet;
        if (optionalTabName != null)
            sheet = wb.getSheet(optionalTabName);
        else
            sheet = wb.getSheet(0);
        if (sheet == null) return null;

        List<Serializable[]> ret = new ArrayList<Serializable[]>();

        for (int i = 0; i < sheet.getRows(); i++)
        {
            Cell[] rowCells = sheet.getRow(i);

            Serializable[] row = new Serializable[rowCells.length];
            for (int j = 0; j < row.length; j++)
            {
                Cell cell = rowCells[j];
                row[j] = getCellValue(cell);
            }
            ret.add(row);
        }
        return ret.toArray(new Serializable[ret.size()][]);
    }

    public static String getCellStringValue(Cell cell, boolean trim)
    {
        Serializable value = getCellValue(cell);
        if (value == null) return "";
        if (value instanceof Date)
            return NumberAndDateFormatUtils.getDateFormatForScripts().format((Date)value);
        if (value instanceof Number)
        {
            String nval = NumberAndDateFormatUtils.getExactPrecisionFormatter().format(value);
            if (nval.endsWith(".0"))
                return nval.replace(".0", "");
            else
                return nval;
        }
        if (value instanceof String && trim)
            return ((String)value).trim();
        return String.valueOf(value);
    }

    public static Serializable getCellValue(Cell cell)
    {
        final CellType cellType = cell.getType();
        if (cellType == CellType.DATE || cellType == CellType.DATE_FORMULA)
        {
            return getDateFromDateCell(cell);
        }
        else if (cellType == CellType.NUMBER || cellType == CellType.NUMBER_FORMULA)
        {
            // note that this can still sometimes produce odd values because of double rounding issues or something
            return ((NumberCell) cell).getValue();
        }
        else
        {
            final String contents = cell.getContents();
            if (isEmpty(contents))
                return null;
            else return contents;
        }
    }

    public static boolean isEmpty(String str)
    {
        return str == null || "".equals(str.trim());
    }

    public static Date getDateFromDateCell(final Cell cell)
    {
        final DateCell dt = (DateCell) cell;
        return dt.getDate();
    }

    public static String[][] readTableFromCSV(final File file,
                                              final boolean filterEmptyAndComments,
                                              final boolean keepHeader,
                                              final boolean trimWhitespace,
                                              final boolean justHeader)
            throws BaseException, IOException
    {
        if (file == null || !file.exists()) return null;

        List<String[]> rows = new ArrayList<String[]>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        if (!keepHeader)
            reader.readLine(); // Discard header
        while(null != (line = reader.readLine()))
        {
            String[] cells = CSVUtils.tokenizeCSVLine(line);
            if (!filterEmptyAndComments || !ConfigManipulator.isRowCommentedOrEmpty(cells))
            {
                if (trimWhitespace)
                    for (int i = 0; i < cells.length; i++)
                        cells[i] = cells[i].trim();
                rows.add(cells);
            }
            if (justHeader) break;
        }
        reader.close();
        return rows.toArray(new String[rows.size()][]);
    }
     */
}
