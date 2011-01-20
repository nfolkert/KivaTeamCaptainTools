package com.nfolkert.utils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.StringReader;

/**
 * A series of utilities for dealing with Strings.
 */
public class TextUtils
{
    /**
     * The system-dependent default separator that appears between lines.
     * * On UNIX systems the value of this field is "\n"; on Win32 systems it is
     * * "\r\n".
     */
    public static final String kLINE_SEP = System.getProperty("line.separator"); // verified static

    /**
     * Two consecutive line separators.
     */
    public static final String kLINE_SEP_DOUBLE = kLINE_SEP + kLINE_SEP;

    /**
     * Separator character that separates elements in a fully-qualified file
     * path (like /home/user/something.txt).  While different platforms do use
     * different characters, we really only care about the separator we use in
     * our system.
     */
//    private static final char kPATH_SEPARATOR = QualifiedNameForPaths.SEPARATOR_SYMBOL;
    public static final char kPATH_SEPARATOR = '/';

    /**
     * When merging a list of values to a String (using
     * {@link #mergeListTogetherWithLimitedSize(java.util.Collection, boolean, String)}),
     * the following value determines how many elements to print if too many
     * items are present in the input.
     */
    private static final int DEFAULT_MERGE_PRINT_OVER = 10;
    /**
     * When merging a list of values to a String (using
     * {@link #mergeListTogetherWithLimitedSize(java.util.Collection, boolean, String)}),
     * the following value determines how many elements to allow before
     * deciding that there are too many.
     */
    private static final int DEFAULT_MERGE_LIMIT = 15;

    /**
     * Private constructor to prevent initialization of this class.
     */
    private TextUtils()
    {
    }

    /**
     * Upper case each word in the phrase.  For example, convert "line item" to
     * "Line Item".  Converts "p&l" to "P&L".
     *
     * More formally, a word is considered to be any contiguous string of
     * letters, digits, hyphens or single quotes.  Everything else is considered
     * a delimiter.
     *
     * Hyphens and single quotes aren't considered delimiters because they are
     * often used in contractions, possessives and compound words.
     *
     * @param phrase, the String to alter.
     * @return the input with each word capitalized.
     */
    public static String upperCaseEachWord(final String phrase)
    {
        boolean isInWord = false;
        final char [] res = new char[phrase.length()];
        for (int i = 0; i < res.length; i++)
        {
            char ch = phrase.charAt(i);
            if (Character.isLetterOrDigit(ch))
            {
                if (! isInWord)
                {
                    ch = Character.toTitleCase(ch);
                    isInWord = true;
                }
            }
            else if ((ch == '\'' || ch == '-') && isInWord)
            {
                // do not exit being in the word--this is a contraction or possesive, or hyphened word
            }
            else isInWord = false;
            res[i] = ch;
        }
        return new String(res);
    }

    /**
     * Merge a list of values together and, if needed, restrict  the number
     * output and include a "...and N others." to the end.
     *
     * @param vals, the list of values to be merged.
     * @param applyLimit, whether or not only a limited number of elements
     *         should be printed.  If false, all the elements (up to a
     *         reasonable maximum) are printed.
     * @param separator, the String to delimit the items in the output.
     * @return the newly-constructed String with the merged output.
     */
    public static String mergeListTogetherWithLimitedSize(final Collection<String> vals, final boolean applyLimit,
                                                          final String separator)
    {
        final StringBuilder builder = new StringBuilder(100);

        mergeListTogetherWithLimitedSize(builder, vals, applyLimit, separator);

        return builder.toString();
    }

    /**
     * Merge a list of values together and, if needed, restrict  the number
     * output and include a "...and N others." to the end.
     *
     * @param sb, the {@link StringBuilder} to which the new list of values
     *         should be appended.
     * @param vals, the list of values to be merged.
     * @param applyLimit, whether or not only a limited number of elements
     *         should be printed.  If false, all the elements (up to a
     *         reasonable maximum) are printed.
     * @param separator, the String to delimit the items in the output.
     */
    public static void mergeListTogetherWithLimitedSize(final StringBuilder sb,
                                                        final Collection< String > vals,
                                                        final boolean applyLimit,
                                                        final String separator)
    {
        final int NUM_TO_PRINT_IF_OVER_MAX = applyLimit ? DEFAULT_MERGE_PRINT_OVER : Integer.MAX_VALUE;
        final int MAX_TO_PRINT = applyLimit ? DEFAULT_MERGE_LIMIT : Integer.MAX_VALUE;
        mergeListTogetherWithLimitedSize(sb, vals, MAX_TO_PRINT, NUM_TO_PRINT_IF_OVER_MAX, separator);
    }

    /**
     * Merge a list of values together and, if needed, restrict  the number
     * output and include a "...and N others." to the end.
     *
     * @param sb, the {@link StringBuilder} to which the new list of values
     *         should be appended.
     * @param vals, the list of values to be merged.
     * @param maxNumToPrint, the maximum number of elements to be printed.
     * @param numToPrintIfOverMax, the number of elements to print if the input
     *         has more than <code>maxNumToPrint</code> elements.  The rest of
     *         the elements will be printed as "...and N others."
     * @param separator, the String to delimit the items in the output.
     */
    public static void mergeListTogetherWithLimitedSize(final StringBuilder sb,
                                                        final Collection< String > vals,
                                                        final int maxNumToPrint,
                                                        final int numToPrintIfOverMax,
                                                        final String separator)
    {


        int numToPrint = vals.size();
        String extraText = "";
        if (numToPrint > maxNumToPrint)
        {
            numToPrint = numToPrintIfOverMax;
            extraText = separator + "...and " + (vals.size() - numToPrint) + " others.";
        }

        sb.ensureCapacity(sb.length() + numToPrint * 30 + 1 + extraText.length());
        int numPrinted = 0;
        for (final Iterator< String > iter = vals.iterator(); numPrinted < numToPrint; numPrinted++)
        {
            if (numPrinted > 0)
                sb.append(separator);

            sb.append(iter.next());
        }
        sb.append(extraText);
    }

    /**
     * Escapes the given string so that it can be used as an argument to a
     * method in the acceptance test framework.
     *
     * Examples:
     *   * basic -> basic
     *   * two words -> "two words"
     *   * with"quote -> "with\"quote"
     *   * with\backslash -> with\\backslash
     *   * null -> null
     *   * (nothing) -> ""
     *
     * @param s, the String to escape and quote.
     * @return the escaped and quoted version of the input.
     */
    public static String escapeAndQuoteForTestArg(String s)
    {
        if (null == s)
        {
            return "null";
        }

        s = StringUtils.replace(s, "\\", "\\\\");
        s = StringUtils.replace(s, "\"", "\\\"");
        if (s.isEmpty() ||
                s.contains(" ") ||
                s.contains("\""))
        {
            // We have to make sure to put quotes if the String is empty,
            // because it would be ignored otherwise.
            s = '"' + s + '"';
        }
        return s;
    }

    /**
     * Trims the given string if it's not null.
     *
     * @param s, the String to trim.
     * @return the trimmed String.  Returns <code>null</code> if s is
     *         <code>null</code>.
     */
    public static String trimSafely(final String s)
    {
        if (s == null)
            return null;
        return s.trim();
    }

    public static String wrapTextPossiblyContainingNewlines(String text)
    {
        text = StringUtils.replace(text, kLINE_SEP, "\n");

        if (!text.contains("\n"))
        {
            return WordUtils.wrap(text, 140, "\n", true);
        }
        else
        {
            final String[] strings = text.split("\n");
            final StringBuffer buf = new StringBuffer(text.length() + 20);

            for (int i = 0; i < strings.length; i++)
            {
                if (i > 0)
                    buf.append("\n");
                buf.append(WordUtils.wrap(strings[i], 140, "\n", true));
            }
            return buf.toString();
        }
    }

    /**
     * Append a piece of text that looks like 1st, 2nd, 3rd, 6th, 41st, 53rd.
     *
     * @param buf, the buffer to which to append the text.
     * @param ord, the number of which to generate the text.
     */
    public static void appendOrdinalDescription(StringBuffer buf, int ord)
    {
        buf.append(ord);
        if (ord % 10 == 1 && ord % 100 != 11) buf.append("st");
        else if (ord % 10 == 2 && ord % 100 != 12) buf.append("nd");
        else if (ord % 10 == 3 && ord % 100 != 13) buf.append("rd");
        else buf.append("th");
    }

    /**
     * Return a piece of text that looks like 1st, 2nd, 3rd, 6th, 41st, 53rd.
     *
     * @param ord, the number of which to generate the text.
     * @return the text with the suffix added.
     */
    public static String getOrdinal(int ord)
    {
        StringBuffer buf = new StringBuffer();
        appendOrdinalDescription(buf, ord);
        return buf.toString();
    }

    public static String escapeForXML(final String s)
    {
        String escapedLabel = s;
        escapedLabel = StringUtils.replace(escapedLabel, "&", "&amp;");
        escapedLabel = StringUtils.replace(escapedLabel, "<", "&lt;");
        escapedLabel = StringUtils.replace(escapedLabel, ">", "&gt;");
        escapedLabel = StringUtils.replace(escapedLabel, "\"", "&quot;");
        return escapedLabel;
    }

    public static String unescapeForXML(final String s)
    {
        String unescaped = s;
        unescaped = StringUtils.replace(unescaped, "&lt;", "<");
        unescaped = StringUtils.replace(unescaped, "&gt;", ">");
        unescaped = StringUtils.replace(unescaped, "&quot;", "\"");
        unescaped = StringUtils.replace(unescaped, "&amp;", "&");
        return unescaped;
    }

    public static String escapeForExcelXML(final String s)
    {
        String escapedLabel = escapeForXML(s);
        escapedLabel = StringUtils.replace(escapedLabel, "\n", "&#10;");
        escapedLabel = StringUtils.replace(escapedLabel, "\r", "&#13;");
        escapedLabel = StringUtils.replace(escapedLabel, "--", "&#45;-");
        return escapedLabel;
    }

    public static String unescapeForExcelXML(final String s)
    {
        String unescaped = unescapeForXML(s);
        unescaped = StringUtils.replace(unescaped, "&#10;", "\n");
        unescaped = StringUtils.replace(unescaped, "&#45;-", "--");
        unescaped = StringUtils.replace(unescaped, "&#13;", "\r" );
        return unescaped;
    }

    public static String escapeForXMLInQuotes(final String s)
    {
        String escapedLabel = s;
        escapedLabel = StringUtils.replace(escapedLabel, "\"", "&quot;");
        return escapedLabel;
    }

    public static String escapeScriptTag(final String s)
    {
        if (StringUtils.isEmpty(s))
        {
            return s;
        }
        else
        {
            String escapedLabel = s.toLowerCase();
            if (!(escapedLabel.matches(".*<.*script.*>.*") ||escapedLabel.matches(".*javascript\\s*:.*")))  // only truly script if in <> or with :, overly strict for nows
            {
                return unescapeHtmlWithExceptions(StringEscapeUtils.escapeHtml(s));
            }
            else
            {
                escapedLabel = StringUtils.replace(escapedLabel, "script", "POSSIBLE INLINE JAVASCRIPT REMOVED");
                return unescapeHtmlWithExceptions(StringEscapeUtils.escapeHtml(escapedLabel));
            }
        }
    }

    private static String unescapeHtmlWithExceptions(String text)
    {
        text = text.replace("&lt;b&gt;","<b>");
        text = text.replace("&lt;/b&gt;","</b>");
        text = text.replace("&lt;br&gt;","<br>");
        text = text.replace("&lt;br/&gt;","<br/>");
        text = text.replace("&lt;i&gt;","<i>");
        text = text.replace("&lt;/i&gt;","</i>");
        text = text.replace("&amp;","&");
        return text;
    }

    /**
     * Merges the input values into a string, printing only the first N and
     * last N elements, putting ellipses in the middle.  Simply prints all the
     * elements if there aren't too many.
     *
     * Printing is done by calling {@link String#toString} on each element.
     *
     * Example:
     *   * ({"horse","cow", "pig", "deer", "moose", "bird }, 2, ",") ->
     *     "horse,cow ... moose,bird"
     *   * ({"horse","cow", "pig", "deer", "moose", "bird }, 3, ",") ->
     *     "horse,cow,pig,moose,bird"
     *
     * @param vals, the values to print into the output.
     * @param numOnFrontAndEnd, the number of elements to print at the front
     *         and end of the input, <em>each</em>.  Thus, twice this many
     *         elements are actually printed.
     * @param separator, the String to use to delimit the elements in the
     *         output.
     * @return the output with the elements merged.
     */
    public static String mergeOrderedListToShowFirstAndLastN(Object[] vals, int numOnFrontAndEnd,
            final String separator)
    {
        if (vals.length <= numOnFrontAndEnd * 2)
            return StringUtils.join(vals, separator);

        StringBuilder res = new StringBuilder(200);

        for (int i = 0; i < numOnFrontAndEnd; i++)
        {
            if ( i > 0 )
                res.append(separator);
            res.append(vals[i]);
        }
        res.append(" ... ");
        for (int i = 0; i < numOnFrontAndEnd; i++)
        {
            if ( i > 0 )
                res.append(separator);
            res.append(vals[vals.length - numOnFrontAndEnd + i]);
        }

        return res.toString();
    }

    /**
     * A thin wrapper around {@link Pattern}.  Contains the pattern itself, and
     * an indication of whether or not the pattern is a {@linkplain #isNegation negation}.
     */
    public static class CompiledPatternForMatch
    {
        /**
         * The compiled pattern.
         */
        public final Pattern pattern;

        /**
         * Whether or not the pattern is a negation.  If it is a negation, then
         * the result of a match using the pattern should be considered the
         * opposite of the actual, desired result.
         */
        public final boolean isNegation;

        /**
         * Sole constructor.
         * @param pattern, the compiled pattern.
         * @param negation, whether or not the pattern is a
         *         {@linkplain #isNegation negation}.
         */
        public CompiledPatternForMatch(Pattern pattern, boolean negation)
        {
            this.pattern = pattern;
            isNegation = negation;
        }
    }

    private static final String sCompileUserEnteredText = "(-?\"(\\\\\"|[^\"])+\"|\\S+)\\s*"; // whitespace seperated words, or quotes (that may contain \")
    private static final Pattern pCompileUserEnteredText = Pattern.compile(sCompileUserEnteredText);

    private static final String sCompileEscape = // verified static
            // a bunch of special characters that normally would have special
            // significance, and so should be escaped
            //   {  }  (  )  !  [  ]  ^  $  +  |
            "([\\{\\}\\(\\)\\!\\[\\]\\^\\$\\+\\|]+" +
                    "|" +
                    // At the beginning of a line, we should escape a backslash
                    // if it doesn't escape a hyphen or a wildcard (hyphen only
                    // has special meaning at the beginning of a pattern).
                    "(?<=^)\\\\(?!-|\\*|\\?)" +
                    "|" +
                    // Anywhere other than the beginning of a line, we should
                    // escape a backslash if it doesn't escape a wildcard.  We
                    // should still escape a backslash in front of a hyphen.
                    "(?<!^)\\\\(?!\\*|\\?))";
    private static final Pattern pCompileEscape = Pattern.compile(sCompileEscape, Pattern.COMMENTS);

    /**
     * Parses a series of limited regular expressions with a simpler format for
     * more common usages.  The format is as follows:
     *
     *  * Each pattern must be separated by spaces.  If there are spaces in the
     *    pattern, the pattern can be quoted with double quotes.
     *  * A pattern can be negated by including a hyphen at the front of each
     *    pattern, outside of the double quotes if there are any.
     *  * "?" stands for any one character, and "*" stands for zero or more
     *    characters.
     *  * Just like in Java's {@link Pattern} format, two backslashes are
     *    needed to escape any of the above special characters because of the
     *    way Java Strings work.  (Obviously, if the user enters this text in,
     *    say, a text box, then this will happen automatically).
     *
     * The result is an array of {@link com.nfolkert.utils.TextUtils.CompiledPatternForMatch}es,
     * which is a thin wrapper around {@link Pattern}.
     *
     * @param toParse, the series of patterns to compile.
     * @return the compiled patterns.
     */
    public static CompiledPatternForMatch [] compileUserEnteredText(String toParse)
    {
        final List<CompiledPatternForMatch> res = new ArrayList<CompiledPatternForMatch>();

        Matcher m = pCompileUserEnteredText.matcher(toParse);

        while (m.find())
        {
            String match = m.group(1).trim();

            boolean isNegated = match.charAt(0) == '-';
            if (isNegated)
            {
                if (match.length() == 1) continue; //blank
                match = match.substring(1);
            }

            if (match.charAt(0) == '"' && match.charAt(match.length() - 1) == '"')
            {
                if (match.length() <= 2) continue; //empty quotes
                match = match.substring(1, match.length() - 1);
            }

            Matcher toEscape = pCompileEscape.matcher(match);
            match = toEscape.replaceAll("\\\\Q$0\\\\E");

            // DOS style patterns
            match = match.replaceAll("(?<!\\\\)\\?", "."); //unescaped ? with .
            match = match.replaceAll("(?<!\\\\)\\*", ".*");  //unescaped * with .*

            final Pattern patt = Pattern.compile(match, Pattern.CASE_INSENSITIVE);
            res.add(new CompiledPatternForMatch(patt, isNegated));

        }

        return res.toArray(new CompiledPatternForMatch[res.size()]);
    }

    public static String wrap(final String input, final int lineWidth)
    {
        if (input.length() <= lineWidth) return input;

        final StringBuilder builder = new StringBuilder(input.length());

        final int numLines = 1 + (input.length() / lineWidth);

        for (int i = 0 ; i < numLines ; i++)
        {
            final int startIndex = i * lineWidth;
            final int endIndex = Math.min(startIndex + lineWidth, input.length());
            builder.append(input.substring(startIndex, endIndex));
            builder.append("\n");
        }

        return builder.toString();
    }

    /**
     * Takes a multiline string and indents each line by a certain number of spaces
     * @param str
     * @param cnt
     * @return
     * @throws IOException
     */
    public static String indent(String str, int cnt)
            throws IOException
    {
        BufferedReader reader = new BufferedReader(new StringReader(str));
        StringBuilder buf = new StringBuilder();

        String indent = "";
        for (int i = 0; i < cnt; i++)
            indent += " ";

        String line;
        while (null != (line = reader.readLine()))
            buf.append(buf.length() == 0 ? "" : "\n").append(indent).append(line);
        return buf.toString();
    }

    /**
     * Takes a String and pluralizes the last word according to the following
     * rules:
     *  * If the String is empty, it will not be changed.
     *  * If the String ends in "y", the last letter is removed and the suffix
     *    "ies" is added.
     *  * If the String ends in "s", it is considered a plural and it is not
     *    changed.
     *  * Otherwise, the suffix "s" is added to the end of the String.
     *
     * @param singular, the String to pluralize.
     * @return the new String with the last word pluralized.
     */
    public static String pluralize(final String singular)
    {
        if (singular.isEmpty())
        {
            return "";
        }
        if (singular.endsWith("y"))
        {
            return singular.substring(0,singular.length() - 1) + "ies";
        }
        if (singular.endsWith("s"))
        {
            return singular;
        }
        return singular + "s";
    }

    /**
     * Takes a fully-qualified name containing a path, and copies over the path
     * to the new name.
     *
     * Examples:
     *  * /home/user/test.txt, test2.txt -> /home/user/test2.txt
     *  * test.txt, test2.txt -> test2.txt (notice no separator)
     *
     * @param nameWithPath, the name containing the path.
     * @param newName, the name to which the path must be applied.
     * @return the newName with the path prepended.
     */
    public static String copyPathNameToNewName(String nameWithPath, String newName)
    {
        String path = extractPathNameFrom(nameWithPath);
        if (0 == path.length())
        {
            return newName;
        }
        return path + kPATH_SEPARATOR + newName;
    }

    /**
     * Takes a fully-qualified name containing a path, extracts and returns
     * just the path.  The path will not contain the last path separator
     * character.
     *
     * If the name is just a file name (i.e. no directories), then an empty
     * String is returned.
     *
     * Examples:
     *  * /home/user/test.txt -> /home/user
     *  * test.txt -> "" // nothing
     *
     * @param nameWithPath, the name containing the path.
     * @return just the path without the last path separator character.
     */
    public static String extractPathNameFrom(String nameWithPath)
    {
        int pathNameExtendsTo = nameWithPath.lastIndexOf(kPATH_SEPARATOR);

        if (pathNameExtendsTo >= 0)
        {
            return nameWithPath.substring(0, pathNameExtendsTo);
        }
        return "";
    }

    /**
     * Takes a fully-qualified name containing a path and removes the path.
     *
     * Example: /home/user/test.txt -> test.txt
     *
     * @param nameWithPath, the name containing the path.
     * @return the name without the path.
     */
    public static String removePathNameFromName(String nameWithPath)
    {
        String[] newNameElements = StringUtils.split(nameWithPath, kPATH_SEPARATOR);

        if (null == newNameElements ||
                0 == newNameElements.length) // the input was empty
        {
            return nameWithPath;
        }
        return newNameElements[newNameElements.length - 1];
    }
}
