package com.nfolkert.collections;

import java.util.*;
import java.io.Serializable;

/**
 * Used for cases when you want to use equals/hashCode lookup simply to convert many objects which are the same
 *   to a single object.
 */
public class HashMapForDeduping<T> implements Serializable
{
    private final HashMap <T,T>_impl;

    public HashMapForDeduping()
    {
        _impl = new HashMap<T, T>();
    }

    public HashMapForDeduping(final int initialCapac)
    {
        _impl = new HashMap<T, T>(initialCapac);
    }

    public void clear()
    {
        _impl.clear();
    }

    public Iterator<T> iterator()
    {
        return _impl.keySet().iterator();
    }

    /**
     *
     * @param obj
     * @return stored obj
     */
    public T remove(final T obj)
    {
        return _impl.remove(obj);
    }

    /**
     * @param coll
     * @return deduped collection
     */
    public Collection<T> addAll( final Collection<T> coll)
    {
        Set<T> res = new HashSet<T>();
        for (T obj : coll)
            res.add(add(obj));
        return res;
    }

    /**
     * Add obj, while deduping
     * @param obj
     * @return deduped obj
     */
    public T add( final T obj )
    {
        T res = _impl.get( obj );
        if ( res == null )
        {
            res = obj;
            _impl.put(res, res);
        }
        return res;
    }

    public int size()
    {
        return _impl.size();
    }

    /**
     * Do a lookup of the given object.  Does NOT modify the map at all.
     * @param o
     */
    public T get(final T o)
    {
        return _impl.get(o);
    }

    public T[] toArray(T[] nodes)
    {
        return _impl.keySet().toArray(nodes);
    }

    public Collection<T> getValues()
    {
        return _impl.values();
    }
}
