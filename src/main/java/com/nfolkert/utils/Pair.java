package com.nfolkert.utils;

import java.io.Serializable;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class Pair< HeadT, TailT> implements Serializable
{
    public final HeadT _head;
    public final TailT _tail;

    public Pair(final HeadT head, final TailT tail)
    {
        _head = head;
        _tail = tail;
    }

    public String toString()
    {
        return String.format("(%s, %s)", _head, _tail);
    }

    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;

        final Pair pair = (Pair) o;

        if (_head != null ? !_head.equals(pair._head) : pair._head != null) return false;
        if (_tail != null ? !_tail.equals(pair._tail) : pair._tail != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (_head != null ? _head.hashCode() : 0);
        result = 29 * result + (_tail != null ? _tail.hashCode() : 0);
        return result;
    }

    /**
     * Do not rename this --it's used in JSP pages
     */
    public HeadT getHead()
    {
        return _head;
    }

    /**
     * Do not rename this --it's used in JSP pages
     */
    public TailT getTail()
    {
        return _tail;
    }

    public static class Of<T> extends Pair<T,T>
    {
        public Of(T head, T tail){
            super(head, tail);
        }
    }

    // Utils

    public static <H,T> Pair<H,T>[] zip(H[] heads, T[] tails)
    {
        Pair<H,T>[] pairs = new Pair[heads.length];
        for(int i = 0; i < pairs.length; i++)
            pairs[i] = new Pair<H,T>(heads[i], tails[i]);

        return pairs;
    }

    public static <H,T> Pair<H,T>[] zip(Pair<H[],T[]> pairs)
    {
        return zip(pairs._head, pairs._tail);
    }

    public static <H,T> Pair<List<H>,List<T>> unzip(Pair<H,T>[] arr)
    {
        return unzip(Arrays.asList(arr));
    }

    public static <H,T> Pair<List<H>,List<T>> unzip(List<? extends Pair<H,T>> arr)
    {
        List<H> heads = new ArrayList<H>();
        List<T> tails = new ArrayList<T>();

        for(Pair<H,T> current : arr)
        {
            heads.add(current._head);
            tails.add(current._tail);
        }
        return new Pair<List<H>,List<T>>(heads, tails);
    }

}
