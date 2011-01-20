package com.nfolkert.utils;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.ArrayList;

import com.nfolkert.exception.BaseException;
import com.nfolkert.exception.BaseRuntimeException;

public class CSVUtils
{
    /**
     * Converts a string representing a CSV line into an array of strings
     * @param line
     * @return
     * @throws BaseException
     */
    public static String[] tokenizeCSVLine(String line)
            throws BaseException
    {
        return tokenizeSymbolSeparatedString(line, ',');
    }

    public static String[] tokenizeSymbolSeparatedString(String line, char separatorSymbol)
        throws BaseException
    {
        if (separatorSymbol == '"')
            throw new BaseRuntimeException("Grouping character cannot be used for separator symbol");
        List<String> tokenList = new ArrayList<String>();

        final int kSTATE_START = 0;
        final int kSTATE_SCAN = 1;
        final int kSTATE_QUOTE = 2;
        final int kSTATE_ENDQUOTE = 3;

        int state = kSTATE_START;
        StringBuilder token = new StringBuilder(30);

        for (int i = 0, n = line.length(); i < n; i++)
        {
            char c = line.charAt(i);

            switch(state)
            {
                case kSTATE_START:
                    if (c == '"')
                    {
                        state = kSTATE_QUOTE;
                        break;
                    }
                    else
                        state = kSTATE_SCAN;
                    // fallthrough
                case kSTATE_SCAN:
                    if (c == '"')
                        throw new BaseException("Badly formed CSV row (embedded quote in unprotected field): \"" + line + "\"");
                    else if (c == separatorSymbol)
                    {
                        state = kSTATE_START;
                        tokenList.add(token.toString());
                        token.setLength(0);
                        break;
                    }
                    token.append(c);
                    break;
                case kSTATE_QUOTE:
                    if (c == '"')
                    {
                        state = kSTATE_ENDQUOTE;
                        break;
                    }
                    token.append(c);
                    break;
                case kSTATE_ENDQUOTE:
                    if (c == '"')
                    {
                        token.append(c);
                        state = kSTATE_QUOTE;
                        break;
                    }
                    if (c == separatorSymbol)
                    {
                        state = kSTATE_START;
                        tokenList.add(token.toString());
                        token.setLength(0);
                        break;
                    }
                    throw new BaseException("Badly formed CSV row (junk following end of quote): \"" + line + "\"");
                default:
                    throw new IllegalStateException();
            }
        }
        if (state == kSTATE_ENDQUOTE || state == kSTATE_SCAN || state == kSTATE_START )
        {
            tokenList.add(token.toString());
        }

        return tokenList.toArray(new String[tokenList.size()]);
    }

    /**
     * Escapes a value for use in a CSV file.  <br>
     * (1) Strings containing commas are quoted in double-quotes.  <br>
     * (2) Double-quote are escaped as ""
     *
     * @param value The value to escape
     * @return The escaped value
     */
    public static String escapeForCSV(String value)
    {
        return escapeForSymbolSeparatedString(value, ',');
    }

    public static String escapeQualifiedNameForCSV(String value)
    {
        return value;
    }

    public static String escapeForSymbolSeparatedString(String value, char symbol)
    {
        if (value == null) return ""; // todo: is this the right thing to do for null strings?  Convert to empty?
        final int quote = value.indexOf('"');
        final int comma = value.indexOf(symbol);
        final int newLine = value.indexOf('\n');
        if (quote >= 0)
            value = StringUtils.replace(value, "\"", "\"\"");
        if (newLine >= 0)
            value = StringUtils.replace(value, "\n", " "); // todo: save these newlines somehow...
        if ((comma >= 0) || (quote >= 0))
        {
            value = "\"" + value + "\"";
        }
        return value;
    }

    public static class Builder
    {
        boolean _first = true;
        final private StringBuilder _builder = new StringBuilder(100);
        final private char _separatorSymbol;

        public Builder(char separatorSymbol)
        {
            if (separatorSymbol == '"') throw new BaseRuntimeException("Grouping character is not allowed as separator character");
            _separatorSymbol = separatorSymbol;
        }

        public void add(String str)
        {
            add(str, true);
        }

        public void add(String str, boolean isQualifiedName)
        {
            if (!_first)
                _builder.append(_separatorSymbol);
            _builder.append(isQualifiedName ? CSVUtils.escapeQualifiedNameForCSV(str)
                    : CSVUtils.escapeForSymbolSeparatedString(str, _separatorSymbol));
            _first = false;
        }

        public void add(Object obj)
        {
            add(obj.toString());
        }

        public String toString()
        {
            return _builder.toString();
        }
    }

    public static String unparse(String[] list, char separatorSymbol)
    {
        return unparse(list, separatorSymbol, false);
    }

    public static String unparse(String[] list, char separatorSymbol, boolean isQualifiedName)
    {
        Builder b = new Builder(separatorSymbol);
        for (String s: list)
        {
            b.add(s, isQualifiedName);
        }
        return b.toString();
    }
}
