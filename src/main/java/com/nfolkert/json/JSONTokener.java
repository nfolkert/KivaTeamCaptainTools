package com.nfolkert.json;

import com.nfolkert.collections.HashMapForDeduping;
import com.nfolkert.utils.FastSimpleStringBufferForCSVProcessorAndJSONParser;
import com.nfolkert.utils.DoubleParserUtilForCSV;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

/*
Copyright (c) 2002 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

/**
 * A JSONTokener takes a source string and extracts characters and tokens from
 * it. It is used by the JSONObject and JSONArray constructors to parse
 * JSON source strings.
 * @author JSON.org
 * @version 2
 */
public class JSONTokener {

    private final HashMapForDeduping< String > _deduper = new HashMapForDeduping<String>(30000);

    /**
     * The index of the next character.
     */
    private int _index;

    /**
     * The source data being tokenized.
     */
    private Reader _source;
    private final FastSimpleStringBufferForCSVProcessorAndJSONParser _builderForNextStuff;

    /**
     * The value to be returned for the next call to next
     */
    private int _cur;

    /**
     * Only set in some cases, this is the next character to take instead of from the reader, or -1 if we should hit the reader.
     */
    private int _next;

    /**
     * The value returned from the previous to call to next
     */
    private int _prev;
    private static final char[] kCHARS_INFINITY = new char[]{'i', 'n', 'f', 'i', 'n', 'i', 't', 'y'};
    private static final char[] kCHARS_FALSE = new char[]{'f', 'a', 'l', 's', 'e'};
    private static final char[] kCHARS_NULL = new char[]{'n', 'u', 'l', 'l'};
    private static final char[] kCHARS_TRUE = new char[]{'t', 'r', 'u', 'e'};
    private static final char[] kCHARS_NAN = new char[]{'n', 'a', 'n'};

    public JSONTokener(String s )
            throws JSONException
    {
        this( new StringReader(s));
    }

    /**
     * Construct a JSONTokener from a string.
     *
     * @param s     A source string.
     */
    public JSONTokener(Reader s)
            throws JSONException
    {
        _index = 0;
        _source = s;
        _cur = readChar(s);
        _next = -1;
        _prev = -1;

        _builderForNextStuff = new FastSimpleStringBufferForCSVProcessorAndJSONParser(100);
    }


    /**
     * Back up one character. This provides a sort of lookahead capability,
     * so that you can test for a digit or letter before attempting to parse
     * the next number or identifier.
     */
    public void back()
    {
        if ( _prev == -1 )
            throw new IllegalStateException("Cannot back twice in a row or from the beginning");
        _index --;
        _next = _cur;
        _cur = _prev;
        _prev = -1;
    }



    /**
     * Get the hex value of a character (base16).
     * @param c A character between '0' and '9' or between 'A' and 'F' or
     * between 'a' and 'f'.
     * @return  An int between 0 and 15, or -1 if c was not a hex digit.
     */
    public static int dehexchar(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return c - ('A' - 10);
        }
        if (c >= 'a' && c <= 'f') {
            return c - ('a' - 10);
        }
        return -1;
    }


    /**
     * Determine if the source string still contains characters that n)
     * can consume.
     * @return true if not yet at the end of the source.
     */
    public boolean more()
    {
        return _cur != -1;
    }


    /**
     * Get the next character in the source string.
     *
     * @return The next character, or 0 if past the end of the source string.
     */
    public char next()
            throws JSONException
    {
        if ( _cur == -1 )
            return 0;

        _prev = _cur;
        if ( _next == -1 )
            _cur = readChar(_source);
        else
        {
            _cur = _next;
            _next = -1;
        }
        _index++;
        return (char) _prev;
    }


    /**
     * Consume the next character, and check that it matches a specified
     * character.
     * @param c The character to match.
     * @return The character.
     * @throws JSONException if the character does not match.
     */
    public char next(char c) throws JSONException {
        char n = next();
        if (n != c) {
            throw syntaxError("Expected '" + c + "' and instead saw '" +
                    n + "'");
        }
        return n;
    }


    /**
     * Get the next n characters.
     *
     * @param n     The number of characters to take.
     * @return      A string of n characters.
     * @throws JSONException
     *   Substring bounds error if there are not
     *   n characters remaining in the source string.
     */
     public String next(int n) throws JSONException
    {
        final char [] res = new char[n];
        for (int i = 0; i < res.length; i++)
            res[i] = next();
         return new String(res);
     }


    /**
     * Get the next char in the string, skipping whitespace
     * and comments (slashslash, slashstar, and hash).
     * @throws JSONException
     * @return  A character, or 0 if there are no more characters.
     */
    public char nextClean() throws JSONException {
        for (;;) {
            char c = next();
            if (c == '/') {
                switch (next()) {
                case '/':
                    do {
                        c = next();
                    } while (c != '\n' && c != '\r' && c != 0);
                    break;
                case '*':
                    for (;;) {
                        c = next();
                        if (c == 0) {
                            throw syntaxError("Unclosed comment");
                        }
                        if (c == '*') {
                            if (next() == '/') {
                                break;
                            }
                            back();
                        }
                    }
                    break;
                default:
                    back();
                    return '/';
                }
            } else if (c == '#') {
                do {
                    c = next();
                } while (c != '\n' && c != '\r' && c != 0);
            } else if (c == 0 || c > ' ') {
                return c;
            }
        }
    }


    /**
     * Return the characters up to the next close quote character.
     * Backslash processing is done. The formal JSON format does not
     * allow strings in single quotes, but an implementation is allowed to
     * accept them.
     * @param quote The quoting character, either
     *      <code>"</code>&nbsp;<small>(double quote)</small> or
     *      <code>'</code>&nbsp;<small>(single quote)</small>.
     * @return      A String.
     * @throws JSONException Unterminated string.
     */
    public String nextString(char quote) throws JSONException {
        char c;
        _builderForNextStuff.setLength(0);
        for (;;) {
            c = next();
            switch (c) {
            case 0:
            case '\n':
            case '\r':
                throw syntaxError("Unterminated string");
            case '\\':
                c = next();
                switch (c) {
                case 'b':
                    _builderForNextStuff.append('\b');
                    break;
                case 't':
                    _builderForNextStuff.append('\t');
                    break;
                case 'n':
                    _builderForNextStuff.append('\n');
                    break;
                case 'f':
                    _builderForNextStuff.append('\f');
                    break;
                case 'r':
                    _builderForNextStuff.append('\r');
                    break;
                case 'u':
                    _builderForNextStuff.append((char)Integer.parseInt(next(4), 16));
                    break;
                case 'x' :
                    _builderForNextStuff.append((char) Integer.parseInt(next(2), 16));
                    break;
                default:
                    _builderForNextStuff.append(c);
                }
                break;
            default:
                if (c == quote) {
                    return _builderForNextStuff.toString();
                }
                _builderForNextStuff.append(c);
            }
        }
    }

    public String nextValueDedup()
            throws JSONException
    {
        final String res = nextValue().toString();
        return _deduper.add(res);
    }


    /**
     * Get the text up but not including the specified character or the
     * end of line, whichever comes first.
     * @param  d A delimiter character.
     * @return   A string.
     */
    public String nextTo(char d)
            throws JSONException
    {
        _builderForNextStuff.setLength(0);
        for (;;) {
            char c = next();
            if (c == d || c == 0 || c == '\n' || c == '\r') {
                if (c != 0) {
                    back();
                }
                return _builderForNextStuff.toString().trim();
            }
            _builderForNextStuff.append(c);
        }
    }


    /**
     * Get the text up but not including one of the specified delimeter
     * characters or the end of line, whichever comes first.
     * @param delimiters A set of delimiter characters.
     * @return A string, trimmed.
     */
    public String nextTo(String delimiters)
            throws JSONException
    {
        char c;
        _builderForNextStuff.setLength(0);
        for (;;) {
            c = next();
            if (delimiters.indexOf(c) >= 0 || c == 0 ||
                    c == '\n' || c == '\r') {
                if (c != 0) {
                    back();
                }
                return _builderForNextStuff.toString().trim();
            }
            _builderForNextStuff.append(c);
        }
    }


    /**
     * Get the next value. The value can be a Boolean, Double, Integer,
     * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
     * @throws JSONException If syntax error.
     *
     * @return An object.
     */
    public Object nextValue() throws JSONException {
        char c = nextClean();

        switch (c) {
            case '"':
            case '\'':
                return nextString(c);
            case '{':
                back();
                return new JSONObject(this);
            case '[':
                back();
                return new JSONArray(this);
        }

        /*
         * Handle unquoted text. This could be the values true, false, or
         * null, or it can be a number. An implementation (such as this one)
         * is allowed to also accept non-standard forms.
         *
         * Accumulate characters until we reach the end of the text or a
         * formatting character.
         */

        _builderForNextStuff.setLength(0);
        boolean hasLetter = false;
        boolean hasDot = false;
        while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0)
        {
            if ( c == ' ')
            {
                // ignore leading spaces
                if ( _builderForNextStuff.length() == 0 )
                    continue;
            }

            if ( ! hasLetter && Character.isLetter(c))
                hasLetter = true;
            if ( ! hasDot && c == '.')
                hasDot = true;
            _builderForNextStuff.append(c);
            c = next();
        }
        back();

        // trim off tailing spaces
        while ( _builderForNextStuff.length() > 0 )
        {
            final int lastIndex = _builderForNextStuff.length() - 1;
            if ( _builderForNextStuff.charAt(lastIndex) == ' ')
                _builderForNextStuff.setLength(lastIndex);
            else break;
        }

        if (_builderForNextStuff.length() == 0)
            throw syntaxError("Missing value");

        /*
         * If it is true, false, or null, return the proper value.
         */
        if ( hasLetter )
        {
            switch (_builderForNextStuff.length())
            {
                case 3:
                    if (_builderForNextStuff.equalsIgnoreCaseNoLengthCheck(kCHARS_NAN))
                        return Double.NaN;
                    break;
                case 4:
                    if (_builderForNextStuff.equalsIgnoreCaseNoLengthCheck(kCHARS_TRUE))
                        return Boolean.TRUE;
                    else if (_builderForNextStuff.equalsIgnoreCaseNoLengthCheck(kCHARS_NULL))
                        return JSONObject.NULL;
                    break;
                case 5:
                    if (_builderForNextStuff.equalsIgnoreCaseNoLengthCheck(kCHARS_FALSE))
                        return Boolean.FALSE;
                    break;
                case 8:
                    if (_builderForNextStuff.equalsIgnoreCaseNoLengthCheck(kCHARS_INFINITY))
                        return Double.POSITIVE_INFINITY;
                    break;
            }
        }

        String s = _builderForNextStuff.toString();

        /*
         * If it might be a number, try converting it. We support the 0- and 0x-
         * conventions. If a number cannot be produced, then the value will just
         * be a string. Note that the 0-, 0x-, plus, and implied string
         * conventions are non-standard. A JSON parser is free to accept
         * non-JSON forms as long as it accepts all correct JSON forms.
         */
        char b = s.charAt(0);
        if ((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+')
        {
            if (b == '0')
            {
                if (s.length() > 2 &&
                        (s.charAt(1) == 'x' || s.charAt(1) == 'X'))
                {
                    try
                    {
                        return Integer.parseInt(s.substring(2), 16);
                    }
                    catch (Exception e)
                    {
                        /* Ignore the error */
                    }
                }
                else try
                {
                    return Integer.parseInt(s, 8);
                }
                catch (Exception e)
                {
                    /* Ignore the error */
                }
            }
            try
            {
                if (hasDot)
                {
                    return DoubleParserUtilForCSV.parseDouble(_builderForNextStuff);
                }
                else
                {
                    if (s.length() > 11)
                        return Long.parseLong(s);
                    else return Integer.parseInt(s);
                }
            }
            catch (Exception e)
            {
                try
                {
                    return Long.parseLong(s);
                }
                catch (Exception f)
                {
                    try
                    {
                        return new Double(s);
                    }
                    catch (Exception g)
                    {
                        return s;
                    }
                }
            }
        }
        return s;
    }

    /**
     * Skip characters until past the requested string.
     * If it is not found, we are left at the end of the source.
     * @param to A string to skip past.
     */
    public boolean skipPast(String to)
            throws JSONException
    {
        char [] find = new char[to.length()];
        to.getChars(0, to.length(), find, 0);
        char [] buf = new char[to.length()];

        for ( ;; )
        {
            int next = next();
            if ( next == 0 )
                return false;
            System.arraycopy(buf, 1, buf, 0, buf.length - 1);
            buf[buf.length - 1] = (char) next;

            if ( Arrays.equals(find,buf))
                return true;
        }
    }


    /**
     * Make a JSONException to signal a syntax error.
     *
     * @param message The error message.
     * @return  A JSONException object, suitable for throwing
     */
    public JSONException syntaxError(String message) {
        return new JSONException(message + toString());
    }


    /**
     * Make a printable string of this JSONTokener.
     *
     * @return " at character [this._index]
     */
    public String toString() {
        return " at character " + _index + " of data.";
    }

    public static int readChar(Reader r)
            throws JSONException
    {
        try
        {
            return r.read();
        }
        catch (IOException e)
        {
            throw new JSONException(e);
        }
    }
}