package com.nfolkert.utils;

/**
 * A simple, unsafe, unsynchronized string buffer that doubles size through copying when needed.
 */
public class FastSimpleStringBufferForCSVProcessorAndJSONParser
{
    private char [] _arr;
    private int _len;

    public FastSimpleStringBufferForCSVProcessorAndJSONParser( int initSize )
    {
        _arr = new char[initSize];
    }

    /**
     * WARNING: may return a value even if index is out of range!
     * @param index
     */
    public char charAt(int index)
    {
        return _arr[index];
    }

    public int length()
    {
        return _len;
    }

    public void append( char ch )
    {
        if ( _len == _arr.length)
            growArray();
        _arr[_len] = ch;
        _len++;
    }

    public void setLength(int len)
    {
        _len = len;
    }

    public String toString()
    {
        return new String( _arr, 0, _len);
    }

    public void append(String s)
    {
        final int strLen = s.length();
        while ( _len + strLen > _arr.length )
            growArray();

        s.getChars( 0, strLen, _arr, _len );
        _len += strLen;
    }

    private void growArray()
    {
        final char [] newArr = new char[_arr.length * 2];
        System.arraycopy( _arr, 0, newArr, 0, _arr.length);
        _arr = newArr;
    }

    public boolean equalsIgnoreCaseNoLengthCheck(char[] charsMustBeLowercase)
    {
        for (int i = 0; i < charsMustBeLowercase.length; i++)
        {
            if ( charsMustBeLowercase[i] != Character.toLowerCase(_arr[i]) )
                return false;
        }
        return true;
    }
}
