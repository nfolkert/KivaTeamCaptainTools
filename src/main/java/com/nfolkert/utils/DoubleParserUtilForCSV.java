package com.nfolkert.utils;

import java.text.ParseException;

/**
 * WARNING WARNING WARNING:
 * Better performance than Double.parseDouble, but does not do error checking as nicely.
 */
public class DoubleParserUtilForCSV
{

    /*
    * All the positive powers of 10 that can be
    * represented exactly in double/float.
    */
    private static final double[] small10pow = { // verified static
            1.0e0,
            1.0e1, 1.0e2, 1.0e3, 1.0e4, 1.0e5,
            1.0e6, 1.0e7, 1.0e8, 1.0e9, 1.0e10,
            1.0e11, 1.0e12, 1.0e13, 1.0e14, 1.0e15,
            1.0e16, 1.0e17, 1.0e18, 1.0e19, 1.0e20,
            1.0e21, 1.0e22
    };
    private static final int maxSmallTen = small10pow.length - 1;
    private static final int maxDecimalDigits = 15;
    private static final int maxDecimalExponent = 308;
    private static final int minDecimalExponent = -324;
    private static final int bigDecimalExponent = 324; // i.e. abs(minDecimalExponent)
    private static final boolean kDEBUG_VERSUS_JAVA_PARSING = false;

    public static double parseDouble(final FastSimpleStringBufferForCSVProcessorAndJSONParser s)
            throws ParseException
    {
        final int strLen = s.length();
        if (strLen == 0)
            return Double.NaN;

        final double toReturn = strLen <= 9 ? parseUsingIntValue(s,strLen) : parseUsingLongValue(s, strLen);
        if (kDEBUG_VERSUS_JAVA_PARSING)
        {
            final double alternative = Double.parseDouble(s.toString());
            if (toReturn != alternative)
                throw new IllegalArgumentException(s.toString() + " parsed differently! " + toReturn + " vs java " + alternative);
        }
        return toReturn;
    }

    private static double parseUsingIntValue(FastSimpleStringBufferForCSVProcessorAndJSONParser s, int strLen)
            throws ParseException
    {
        final boolean isNegative;
        int i;
        if ( s.charAt(0) == '-' )
        {
            isNegative = true;
            i = 1;
        }
        else
        {
            isNegative = false;
            i = 0;
        }
        int intValue = 0;
        int exp = 0;
        int expDelta = 0;
        int numDigits = 0;
        for (; i < strLen; i++)
        {
            final char ch = s.charAt(i);
            if (ch == '.')
            {
                if (expDelta == -1)
                    throw new ParseException("Failed to parse '" + s.toString() + "' at offset " + i, i);
                expDelta = -1;
            }
            else if ( ch == 'E')
            {
                exp += parseInt(s,i+1,strLen);
                i = strLen;
            }
            else if (ch < '0' || ch > '9')
            {
                throw new ParseException("Failed to parse '" + s.toString() + "' at offset " + i, i);
            }
            else
            {
                int val = ch - '0';
                intValue = intValue * 10 + val;
                exp += expDelta;
                numDigits++;
            }
        }

        if (numDigits == 0)
            throw new ParseException("Failed to parse '" + s.toString() + "'", 0);

        // todo: we can probably optimize converToDouble knowing that it's of limited size.
        return convertToDouble(intValue, exp, isNegative, numDigits);
    }

    private static int parseInt(FastSimpleStringBufferForCSVProcessorAndJSONParser s, int startIndex, int strLen)
            throws ParseException
    {
        if ( startIndex == strLen)
            throw new ParseException("Failed to parse '" + s.toString() + "' at offset " + startIndex, startIndex);

        int res = 0;
        final boolean isNegative;
        int i = startIndex;
        if ( s.charAt(i) == '-' )
        {
            isNegative = true;
            i++;
        }
        else
        {
            isNegative = false;
        }
        int intValue = 0;
        for ( ; i < strLen; i++)
        {
            final char ch = s.charAt(i);
            if (ch < '0' || ch > '9')
            {
                throw new ParseException("Failed to parse '" + s.toString() + "' at offset " + i, i);
            }
            else
            {
                int val = ch - '0';
                intValue = intValue * 10 + val;
            }
        }
        return isNegative ? (-1 * intValue) : intValue;
    }


    private static double parseUsingLongValue(FastSimpleStringBufferForCSVProcessorAndJSONParser s, int strLen)
            throws ParseException
    {
        // If the string is ridiculously long, long type is not enough to hold the mantissa.
        // In such case, use java built in double parser as we are not likely to see those numbers anyway...
        if (strLen >= 18)
            return Double.parseDouble(s.toString());

        final boolean isNegative;
        int i;
        if ( s.charAt(0) == '-' )
        {
            isNegative = true;
            i = 1;
        }
        else
        {
            isNegative = false;
            i = 0;
        }
        long longValue = 0;
        int exp = 0;
        int expDelta = 0;
        int numDigits = 0;
        for (; i < strLen; i++)
        {
            final char ch = s.charAt(i);
            if (ch == '.')
            {
                if (expDelta == -1)
                    throw new ParseException("Failed to parse '" + s.toString() + "' at offset " + i, i);
                expDelta = -1;
            }
            else if ( ch == 'E')
            {
                exp += parseInt(s,i+1,strLen);
                i = strLen;
            }
            else if (ch < '0' || ch > '9')
            {
                throw new ParseException("Failed to parse '" + s.toString() + "' at offset " + i, i);
            }
            else
            {
                int val = ch - '0';
                longValue = longValue * 10 + val;
                exp += expDelta;
                numDigits++;
            }
        }

        if (numDigits == 0)
            throw new ParseException("Failed to parse '" + s.toString() + "'", 0);

        return convertToDouble((double) longValue, exp, isNegative, numDigits);
    }

    /**
     * @param dValue the value of the digits, without scale, as a double
     * @param exp
     * @param isNegative
     * @param nDigits
     * @return
     * @throws ParseException
     */
    private static double convertToDouble(double dValue, int exp, boolean isNegative, int nDigits)
            throws ParseException
    {
        // taken from FloatingDecimal and modified for our case
        /*
        * possibly an easy case.
        * We know that the digits can be represented
        * exactly. And if the exponent isn't too outrageous,
        * the whole thing can be done with one operation,
        * thus one rounding error.
        * Note that all our constructors trim all leading and
        * trailing zeros, so simple values (including zero)
        * will always end up here
        */
        if (exp == 0 || dValue == 0.0)
        {
            return (isNegative) ? -dValue : dValue; // small floating integer
        }
        else if (exp >= 0)
        {
            if (exp <= maxSmallTen)
            {
                /*
                * Can get the answer with one operation,
                * thus one roundoff.
                */
                double rValue = dValue * small10pow[exp];
                return (isNegative) ? -rValue : rValue;
            }

            int kDigits = Math.min(nDigits, maxDecimalDigits + 1);
            int slop = maxDecimalDigits - kDigits;
            if (exp <= maxSmallTen + slop)
            {
                /*
                * We can multiply dValue by 10^(slop)
                * and it is still "small" and exact.
                * Then we can multiply by 10^(exp-slop)
                * with one rounding.
                */
                dValue *= small10pow[slop];
                double rValue = dValue * small10pow[exp - slop];

                return (isNegative) ? -rValue : rValue;
            }
            else
            {
                /*
                * Overflow
                */
                return isNegative? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            }
        }
        else
        {
            if (exp >= -maxSmallTen)
            {
                /*
                * Can get the answer in one division.
                */
                double rValue = dValue / small10pow[-exp];
                return (isNegative) ? -rValue : rValue;
            }
            else
            {
                /*
                * Underflow
                */
                return isNegative? -0 : 0;
            }
        }
    }
}
