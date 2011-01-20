package com.nfolkert.utils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.*;

/**
 */
public class StringUtilsExt
{
    public static final String kCHARSET_UTF8 = "UTF-8";
    public static final String kCHARSET_ASCII = "ASCII";

    private StringUtilsExt(){}

    /**
     * Returns false if either parameter is null
     */
    public static boolean startsWithIgnoreCase( final String s, final String prefix )
    {
        if ( s == null || prefix == null )
            return false;

        final int prefixLen = prefix.length();
        if ( s.length() < prefixLen)
            return false;

        for ( int i = 0; i < prefixLen; i++)
        {
            if ( Character.toLowerCase( s.charAt(i) ) !=
                 Character.toLowerCase( prefix.charAt(i)))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * IF the given string begins with the given prefix (case-insensitive compare), then the
     *   substring after prefix is returned.  Otherwise, the entire given string is returned.
     *
     */
    public static String stripPrefixIgnoreCase( final String s, final String prefix )
    {
        if ( startsWithIgnoreCase( s, prefix ))
            return s.substring( prefix.length());
        else return s;
    }

    /**
     * Example usage:  errorMessageFromList("The following {0} undefined: {1}", undefinedObjs,
     *                 ",", "dimension is", "dimensions are");
     *
     * @param messageWithTwoPlaceholders 1st placeholder is the singular/plural object type name, second
     *               placeholder is the list of objects
     * @param objectsToList The objects to list -- should have good toString()
     * @param separator Delimiter to use in the object list
     * @param singularObjectTypeName Used for placeholder {0} if objectsToList.size() == 1
     * @param pluralObjectTypeName Used for placeholder {0} if objectsToList.size() != 1
     * @param maxNumberToInclude If there are more than this number, then the first (maxNumberToInclude - 5)
     *             are included along with "and XX others".

     * @return Error message
     */
    public static String errorMessageFromList(String messageWithTwoPlaceholders, Collection objectsToList,
                                              String separator, String singularObjectTypeName,
                                              String pluralObjectTypeName, int maxNumberToInclude)
    {
        final String objectTypeName =
                (objectsToList != null && objectsToList.size() == 1) ? singularObjectTypeName : pluralObjectTypeName;
        return MessageFormat.format(messageWithTwoPlaceholders, new Object[] {
                objectTypeName, listOfObjectLabels(objectsToList, separator, maxNumberToInclude)
        });
    }

    /**
     * @param objectsToList The objects to list -- should have good toString()
     * @param separator Delimiter to use in the object list
     * @param maxNumberToInclude If there are more than this number, then the first (maxNumberToInclude - 5)
     *             are included along with "and XX others".

     * @return List of object labels, delimited by seprator.
     */
    public static String listOfObjectLabels(Collection objectsToList, String separator, int maxNumberToInclude)
    {
        final int size = objectsToList.size();
        maxNumberToInclude = Math.max(maxNumberToInclude, 5);
        final boolean tooMany = size > maxNumberToInclude;
        final int numToList = tooMany ? maxNumberToInclude - 5 : size;
        final StringBuffer buf = new StringBuffer();
        final Iterator it = objectsToList.iterator();
        boolean first = true;
        for (int i = 0; i < numToList; i++)
        {
            if (first)
                first = false;
            else
                buf.append(separator);
            buf.append(it.next());
        }

        if (tooMany)
            buf.append(separator).append("and ").append(size - numToList).append(" others");

        return buf.toString();
    }

    /**
     * Return the current line, with newlines replaced with <br> entries, and each line itself escaped for html.
     * @param line
     */
    public static String createMultilineForHTML( final String line )
    {
        final String[] strs = splitIntoLines(line);
        for (int i = 0; i < strs.length; i++)
            strs[i] = StringEscapeUtils.escapeHtml(strs[i]);
        return StringUtils.join( strs, "<br>" );
    }

    /**
     * Split into multiple lines, counting both \r\n and \n as separators.
     *
     * Unlike StringUtils.split, this does return empty lines (consecutive separators are not treated as one).
     * @param contents
     */
    public static String[] splitIntoLines(String contents)
    {
        // man this is goofy--but we need to not merge adjacent tokens.  // todo: fix this hackiness
        contents = contents.replaceAll( "\r", "" );
        contents = contents.replaceAll( "\n", "\r\n" );
        final String[] res = StringUtils.split(contents, '\n');
        for (int i = 0; i < res.length; i++)
        {
            res[i] = res[i].replaceAll( "\r", "" );
        }
        return res;
    }

    /**
     * Removes any lines that are blank according to {@link StringUtils#isBlank}.
     * Counts both \r\n and \n as separators, but the return value will only
     * contain \n as the separator.
     *
     * @param contents, the String from which to remove the blank lines.
     * @return the input without blank lines.
     */
    public static String removeBlankLines(String contents)
    {
        String[] lines = splitIntoLines(contents);
        List<String> linesNoBlanks = new ArrayList<String>(lines.length);

        for (String line : lines)
        {
            if (StringUtils.isNotBlank(line))
            {
                linesNoBlanks.add(line);
            }
        }

        return StringUtils.join(linesNoBlanks.toArray(), '\n');
    }

    /**
     * Unlike StringUtils.split, this will return an empty string in the result for each double separator.
     *
     * So with a common separator:
     *
     *    horse,,cow  will return { "horse", "", "cow" }
     *    pig,,,latin, will return { "pig", "", "", "latin", "" }
     */
    public static String[] splitWithDoubleSeparatorTreatment(final String str, final char separatorChar)
    {
        // Performance tuned for 2.0 (JDK1.4)

        if (str == null)
        {
            return null;
        }
        int len = str.length();
        if (len == 0)
        {
            return org.apache.commons.lang.ArrayUtils.EMPTY_STRING_ARRAY;
        }
        List<String> list = new ArrayList<String>();
        int i = 0, start = 0;
        boolean match = false;
        while (i < len)
        {
            if (str.charAt(i) == separatorChar)
            {
                if (match)
                {
                    list.add(str.substring(start, i));
                    match = false;
                }
                else
                    list.add( "" );
                start = ++i;
                continue;
            }
            match = true;
            i++;
        }
        if (match)
        {
            list.add(str.substring(start, i));
        }
        else if ( start > 0 )
            list.add( "" );

        return (String[]) list.toArray(new String[list.size()]);
    }


    /**
     * Performs null-safe compare -- null comes before non-null.  Same as left.compareTo(right) if both are non-null.
     */
    public static int compareTo(String left, String right)
    {
        if (left == null)
            return right == null ? 0  : -1;
        if (right == null)
            return 1;
        return left.compareTo(right);
    }

    /**
     * Performs null-safe compare -- null comes before non-null.  Same as left.compareToIgnoreCase(right) if both are non-null.
     */
    public static int compareToIgnoreCase(String left, String right)
    {
        if (left == null)
            return right == null ? 0  : -1;
        if (right == null)
            return 1;
        return left.compareToIgnoreCase(right);
    }

    /**
     * return a string containing <code>numToInclude</code> copies of <code>toExplode</code>, or null if <code>toExplode</code> is null
     * @param toExplode
     * @param numToInclude
     */
    public static String explode(final String toExplode, int numToInclude)
    {
        if ( toExplode == null )
            return null;
        final int strLen = toExplode.length();
        final StringBuffer res = new StringBuffer(strLen * numToInclude + 1 );
        for ( int i = 0; i < numToInclude; i++)
            res.append( toExplode );
        return new String(res);
    }

    /**
     * Split and count successive tokens as being separated by empty string.  In addition, a leading delimiter
     *   acts as though preceded by an empty string.  A trailing delimiter acts as though succeeded by an empty string
     *
     * So, with a comma delimiter:
     *
     * ,,X  will return an array of { "", "", "X" }
     * X,,  will return an array of { "X", "", "" }
     * ,X,  will return an array of { "", "X", "" }
     * X,X,X  will return an array of { "X", "X", "X" }
     */
    public static String[] splitCountingSeparateDelimiters(String val, String delim)
    {
        boolean onDelim = true;
        final ArrayList<String> res = new ArrayList<String>(31);
        for ( final StringTokenizer tok = new StringTokenizer(val, delim, true);
            tok.hasMoreElements(); )
        {
            final String token = tok.nextToken();
            if ( token.equals( delim ))
            {
                if ( onDelim )
                    res.add( "" );
                onDelim = true;
            }
            else
            {
                res.add( token );
                onDelim = false;
            }
        }
        if ( onDelim )
            res.add( "" );
        return (String[]) res.toArray(new String[res.size()]);
    }

    /**
     *
     * @param val
     * @return if val is null, then the string "null" is returned.  Otherwise, <code>"'" + escapeForJS(val) + "'"</code> is returned.
     */
    public static String escapeAndQuoteForJS( final String val )
    {
        if ( val == null )
            return "null";
        else return "'" + StringEscapeUtils.escapeJavaScript(val) + "'";
    }

    public static String blankToNull(final String name)
    {
        return name == null || name.equals("") ? null : name;
    }

    public static String grammaticalArrayJoin(Object... vals)
    {
        return grammaticalJoin(',', Arrays.asList(vals));
    }

    public static String grammaticalArrayJoinWithEndTerm(Character separator, String endTerm, Object... vals)
    {
        return grammaticalJoin(separator, endTerm, Arrays.asList(vals));
    }

    public static String grammaticalArrayJoin(Character separator, Object... vals)
    {
        return grammaticalJoin(separator, Arrays.asList(vals));
    }

    public static String grammaticalJoin(List vals)
    {
        return grammaticalJoin(',', vals);
    }

    public static String grammaticalJoin(Character separator, List vals)
    {
        return grammaticalJoin(separator, "and", vals);
    }

    public static String grammaticalJoin(Character separator, String endTerm, List vals)
    {
        StringBuilder buf = new StringBuilder();

        int len = vals.size();
        for (int i = 0; i < len; i++)
        {
            if (i > 1 && i == len - 1) buf.append(separator).append(" ").append(endTerm).append(" ");
            else if (i > 0 && i == len - 1) buf.append(" ").append(endTerm).append(" ");
            else if (i > 0 && len > 2) buf.append(separator).append(" ");

            buf.append(vals.get(i));
        }
        return buf.toString();
    }

    public static void removeEmptyStringsFromList(/*in-out*/final List<String> list)
    {
        for (final Iterator<String> it = list.iterator(); it.hasNext(); )
        {
            final String s = it.next();
            if (StringUtils.isEmpty(s)) it.remove();
        }
    }

    public static String removeCharacters(String str, final String toRemove)
    {
        for (final char c : toRemove.toCharArray())
        {
            str = StringUtils.remove(str, c);
        }

        return str;
    }

    /**
     * Remove all non-alphanumeric characters from the string
     * @param src
     * @return
     */
    public static String alphanumericize(String src)
    {
        if (src == null) return null;
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < src.length(); i++)
        {
            final char c = src.charAt(i);
            if (Character.isLetterOrDigit(c))
                buf.append(c);
        }
        return buf.toString();
    }

    // useful for partially outputting stack traces (e.g. tests, logging)
    public static String trimToNLines(final String input, final int n)
    {
            int lastIndex = 0;
            for (int i = 0 ; i < n ; i++)
            {
                lastIndex = input.indexOf("\n", lastIndex);
                if (lastIndex == -1) return input;
            }

            return input.substring(0, lastIndex);
    }

    /**
     * @param input
     * @param name
     * @return true if after removing all non-alphanumeric chars, the strings are equal, ignoring case
     */
    public static boolean equalsAlphanumIgnoreCase(String name, String input)
    {
        if (input == null) return false;
        String replIn = input.replaceAll("\\W", "").replaceAll("_", "");
        String replName = name.replaceAll("\\W", "").replaceAll("_", "");
        return replName.equalsIgnoreCase(replIn);
    }

    // Note that line is zero indexed
    public static int getLineNumber(String text, int position)
    {
        int lineNumber = 0;
        for (int i = 0; i < position; i++)
        {
            if (text.charAt(i) == '\n')
                lineNumber++;
        }
        return lineNumber;
    }

    // Note that line is zero indexed
    public static int getPosForLine(String text, int line)
    {
        int pos;
        int curLine = 0;
        for (pos = 0; pos < text.length(); pos++)
        {
            if (text.charAt(pos) == '\n')
                curLine++;
            if (curLine == line)
                break;
        }
        return Math.min(text.length()-1, pos+1);
    }
}
