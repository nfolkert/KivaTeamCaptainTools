/*
 *  Copyright 2003-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.nfolkert.collections;

import java.util.*;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.apache.commons.collections.iterators.AbstractIteratorDecorator;

/**
 * Taken from the Jakarta commons-collection and augmented to have generics
 *
 * Decorates another <code>Set</code> to ensure that the order of addition
 * is retained and used by the iterator.
 * <p>
 * If an object is added to the set for a second time, it will remain in the
 * original position in the iteration.
 * The order can be observed from the set via the iterator or toArray methods.
 * <p>
 * The ListOrderedSet also has various useful direct methods. These include many
 * from <code>List</code>, such as <code>get(int)</code>, <code>remove(int)</code>
 * and <code>indexOf(int)</code>. An unmodifiable <code>List</code> view of
 * the set can be obtained via <code>asList()</code>.
 * <p>
 * This class cannot implement the <code>List</code> interface directly as
 * various interface methods (notably equals/hashCode) are incompatable with a set.
 * <p>
 * This class is Serializable from Commons Collections 3.1.
 *
 * @since Commons Collections 3.0
 * @version $Revision: 155406 $ $Date: 2005-02-26 12:55:26 +0000 (Sat, 26 Feb 2005) $
 *
 * @author Stephen Colebourne
 * @author Henning P. Schmiedehausen
 */
public class ListOrderedSet<T> implements Set<T>, Serializable
{
    /** Serialization version */
    private static final long serialVersionUID = -228664372470420141L;

    /** Internal list to hold the sequence of objects */
    protected List<T> _order;

    /** The collection being decorated */
    protected Set<T> _set;

    /**
     * Factory method to create an ordered set specifying the list and set to use.
     * <p>
     * The list and set must both be empty.
     *
     * @param set  the set to decorate, must be empty and not null
     * @param list  the list to decorate, must be empty and not null
     * @throws IllegalArgumentException if set or list is null
     * @throws IllegalArgumentException if either the set or list is not empty
     * @since Commons Collections 3.1
     */
    public static <T> ListOrderedSet<T> decorate(Set<T> set, List<T> list) {
        if (set == null) {
            throw new IllegalArgumentException("Set must not be null");
        }
        if (list == null) {
            throw new IllegalArgumentException("List must not be null");
        }
        if (set.size() > 0 || list.size() > 0) {
            throw new IllegalArgumentException("Set and List must be empty");
        }
        return new ListOrderedSet<T>(set, list);
    }

    /**
     * Factory method to create an ordered set.
     * <p>
     * An <code>ArrayList</code> is used to retain order.
     *
     * @param set  the set to decorate, must not be null
     * @throws IllegalArgumentException if set is null
     */
    public static ListOrderedSet decorate(Set set) {
        return new ListOrderedSet(set);
    }

    /**
     * Factory method to create an ordered set using the supplied list to retain order.
     * <p>
     * A <code>HashSet</code> is used for the set behaviour.
     * <p>
     * NOTE: If the list contains duplicates, the duplicates are removed,
     * altering the specified list.
     *
     * @param list  the list to decorate, must not be null
     * @throws IllegalArgumentException if list is null
     */
    public static ListOrderedSet decorate(List list) {
        if (list == null) {
            throw new IllegalArgumentException("List must not be null");
        }
        Set set = new HashSet(list);
        list.retainAll(set);

        return new ListOrderedSet(set, list);
    }

    //-----------------------------------------------------------------------
    /**
     * Constructs a new empty <code>ListOrderedSet</code> using
     * a <code>HashSet</code> and an <code>ArrayList</code> internally.
     *
     * @since Commons Collections 3.1
     */
    public ListOrderedSet()
    {
        this ( new HashSet<T>(), new ArrayList<T>());
    }

    public ListOrderedSet(int initialSize)
    {
        this(new HashSet<T>(initialSize), new ArrayList<T>(initialSize));
    }

    public ListOrderedSet(Collection<T> initial)
    {
        this(initial.size());
        addAll(initial);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param set  the set to decorate, must not be null
     * @throws IllegalArgumentException if set is null
     */
    protected ListOrderedSet(Set<T> set) {
        this( set, new ArrayList<T>(set));
    }

    public Map<T, Integer> indexMap()
    {
        Map<T, Integer> indexMap = new HashMap<T, Integer>();
        for (int i = 0, n = size(); i < n; i++)
            indexMap.put(get(i), i);
        return indexMap;
    }

    /**
     * Constructor that wraps (not copies) the Set and specifies the list to use.
     * <p>
     * The set and list must both be correctly initialised to the same elements.
     *
     * @param set  the set to decorate, must not be null
     * @param list  the list to decorate, must not be null
     * @throws IllegalArgumentException if set or list is null
     */
    protected ListOrderedSet(Set<T> set, List<T> list)
    {
        if (set == null)
            throw new IllegalArgumentException("Set must not be null");
        if (list == null)
            throw new IllegalArgumentException("List must not be null");
        _set = set;
        _order = list;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets an unmodifiable view of the order of the Set.
     *
     * @return an unmodifiable list view
     */
    public List<T> asList() {
        return Collections.unmodifiableList(_order);
    }

    //-----------------------------------------------------------------------
    public void clear()
    {
        _set.clear();
        _order.clear();
    }

    public Iterator<T> iterator() {
        return new OrderedSetIterator(_order.iterator(), _set);
    }

    public boolean add(T object) {
        if (_set.contains(object)) {
            // re-adding doesn't change order
            return _set.add(object);
        } else {
            // first add, so add to both set and list
            boolean result = _set.add(object);
            _order.add(object);
            return result;
        }
    }

    public boolean addAll(Collection<? extends T> coll) {
        boolean result = false;
        for (final Iterator<? extends T> it = coll.iterator(); it.hasNext();) {
            T object = it.next();
            result = result | add(object);
        }
        return result;
    }

    public boolean remove(Object object) {
        boolean result = _set.remove(object);
        _order.remove(object);
        return result;
    }

    public boolean removeAll(Collection< ? > coll) {
        boolean result = false;
        for (Iterator< ? > it = coll.iterator(); it.hasNext();) {
            Object object = it.next();
            result = result | remove(object);
        }
        return result;
    }

    public boolean retainAll(Collection coll) {
        boolean result = _set.retainAll(coll);
        if (result == false) {
            return false;
        } else if (_set.size() == 0) {
            _order.clear();
        } else {
            for (Iterator it = _order.iterator(); it.hasNext();) {
                Object object = it.next();
                if (_set.contains(object) == false) {
                    it.remove();
                }
            }
        }
        return result;
    }

    public Object[] toArray()
    {
        return _order.toArray();
    }

    //-----------------------------------------------------------------------
    public T get(int index) {
        return _order.get(index);
    }

    public int indexOf(T object) {
        return _order.indexOf(object);
    }

    public void add(int index, T object) {
        if (! contains(object)) {
            _set.add(object);
            _order.add(index, object);
        }
    }

    public boolean contains(Object object)
    {
        return _set.contains(object);
    }

    public boolean containsAll(Collection<?> c)
    {
        return _set.containsAll(c);
    }

    public boolean isEmpty()
    {
        return _set.isEmpty();
    }

    public <T>T[] toArray(T[] a)
    {
        return _order.toArray(a);
    }

    public boolean addAll(int index, Collection< ? extends T> coll) {
        boolean changed = false;
        for (Iterator<? extends T> it = coll.iterator(); it.hasNext();) {
            T object = it.next();
            if (! contains(object))
            {
                _set.add(object);
                _order.add(index, object);
                index++;
                changed = true;
            }
        }
        return changed;
    }

    public int size()
    {
        return _set.size();
    }

    public T remove(int index) {
        T obj = _order.remove(index);
        remove(obj);
        return obj;
    }

    /**
     * Uses the underlying List's toString so that order is achieved.
     * This means that the decorated Set's toString is not used, so
     * any custom toStrings will be ignored.
     */
    // Fortunately List.toString and Set.toString look the same
    public String toString() {
        return _order.toString();
    }

    //-----------------------------------------------------------------------
    /**
     * Internal iterator handle remove.
     */
    static class OrderedSetIterator<T> extends AbstractIteratorDecorator {

        /** Object we iterate on */
        protected final Collection<T> set;
        /** Last object retrieved */
        protected T last;

        private OrderedSetIterator(Iterator<T> iterator, Collection<T> set) {
            super(iterator);
            this.set = set;
        }

        public T next() {
            last = (T) iterator.next();
            return last;
        }

        public void remove() {
            set.remove(last);
            iterator.remove();
            last = null;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Write the set out using a custom routine.
     *
     * @param out  the output stream
     * @throws java.io.IOException
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(_set);
        out.writeObject(_order);
    }

    /**
     * Read the set in using a custom routine.
     *
     * @param in  the input stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        _set = (Set<T>) in.readObject();
        _order = (List<T>) in.readObject();
    }

    // todo: should base equality and hashcode on set, not order (this does not implement list anyway)
    public boolean equals(Object object) {
        if (object == this)
            return true;
        return _order.equals(object);
    }

    public int hashCode() {
        return _order.hashCode();
    }
}
